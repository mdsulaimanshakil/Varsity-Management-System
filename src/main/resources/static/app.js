
const COMMON_FIELDS = [
    { key: 'fullName', label: 'Full name', required: true },
    { key: 'username', label: 'Username', required: true },
    { key: 'email', label: 'Email', type: 'email' },
    { key: 'department', label: 'Department' },
    { key: 'password', label: 'Password', type: 'password', table: false },
];


const RESOURCES = {
    admins: { label: 'Admins', singular: 'Admin', roles: ['ADMIN'], fields: [] },
    teachers: { label: 'Teachers', singular: 'Teacher', roles: ['ADMIN', 'TEACHER'], fields: [] },
    students: { label: 'Students', singular: 'Student', roles: ['ADMIN', 'TEACHER', 'STUDENT'], fields: [] },
};

const $ = (id) => document.getElementById(id);
const state = { user: null, resource: null, rows: [], editingId: null };


async function api(path, options = {}) {
    const res = await fetch(path, {
        credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
        ...options,
    });
    if (res.status === 204) return null;
    const body = await res.json().catch(() => null);
    if (!res.ok) {
        const err = new Error(body?.message || `Request failed (${res.status})`);
        err.status = res.status;
        throw err;
    }
    return body;
}

async function boot() {
    try {
        state.user = await api('/api/auth/me');
        showApp();
    } catch {
        showLogin();
    }
}

function showLogin() {
    $('app-view').hidden = true;
    $('login-view').hidden = false;
    $('login-form').reset();
}

$('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = new FormData(e.target);
    $('login-error').hidden = true;
    try {
        const res = await fetch('/api/auth/login', { method: 'POST', body: new URLSearchParams(form) });
        if (!res.ok) throw new Error('Invalid username or password');
        state.user = await api('/api/auth/me');
        showApp();
    } catch (err) {
        $('login-error').textContent = err.message;
        $('login-error').hidden = false;
    }
});

$('logout-btn').addEventListener('click', async () => {
    await api('/api/auth/logout', { method: 'POST' }).catch(() => {});
    state.user = null;
    showLogin();
});

// ---------- Dashboard ----------
function showApp() {
    $('login-view').hidden = true;
    $('app-view').hidden = false;
    $('user-name').textContent = state.user.fullName;
    $('user-role').textContent = state.user.role;

    const allowed = Object.keys(RESOURCES).filter((k) => RESOURCES[k].roles.includes(state.user.role));
    $('tabs').innerHTML = '';
    allowed.forEach((key) => {
        const btn = document.createElement('button');
        btn.textContent = RESOURCES[key].label;
        btn.dataset.key = key;
        btn.addEventListener('click', () => selectResource(key));
        $('tabs').appendChild(btn);
    });
    selectResource(allowed[0]);
}

async function selectResource(key) {
    state.resource = key;
    [...$('tabs').children].forEach((b) => b.classList.toggle('active', b.dataset.key === key));
    $('resource-title').textContent = RESOURCES[key].label;
    $('add-btn').textContent = `+ Add ${RESOURCES[key].singular}`;
    $('search').value = '';
    await loadRows();
}

async function loadRows() {
    try {
        state.rows = await api(`/api/${state.resource}`);
        render();
    } catch (err) {
        if (err.status === 401) return showLogin();
        toast(err.message);
    }
}

function columns() {
    return [...COMMON_FIELDS, ...RESOURCES[state.resource].fields].filter((f) => f.table !== false);
}

function render() {
    const cols = columns();
    const q = $('search').value.trim().toLowerCase();
    const rows = state.rows.filter((r) => !q || cols.some((c) => String(r[c.key] ?? '').toLowerCase().includes(q)));

    $('table-head').innerHTML =
        `<tr><th>ID</th>${cols.map((c) => `<th>${esc(c.label)}</th>`).join('')}<th></th></tr>`;
    $('table-body').innerHTML = rows.map((r) => `
        <tr>
            <td>${r.id}</td>
            ${cols.map((c) => `<td>${esc(r[c.key] ?? '—')}</td>`).join('')}
            <td class="actions">
                <button class="btn link" data-edit="${r.id}">Edit</button>
                <button class="btn link danger" data-delete="${r.id}">Delete</button>
            </td>
        </tr>`).join('');
    $('empty').hidden = rows.length > 0;
    const n = state.rows.length;
    $('resource-count').textContent = `${n} record${n === 1 ? '' : 's'}`;
}

$('search').addEventListener('input', render);

$('table-body').addEventListener('click', async (e) => {
    const editId = e.target.dataset.edit;
    const deleteId = e.target.dataset.delete;
    if (editId) openEditor(state.rows.find((r) => String(r.id) === editId));
    if (deleteId) {
        const row = state.rows.find((r) => String(r.id) === deleteId);
        if (!confirm(`Delete ${row.fullName}? This cannot be undone.`)) return;
        try {
            await api(`/api/${state.resource}/${deleteId}`, { method: 'DELETE' });
            toast(`${RESOURCES[state.resource].singular} deleted`);
            loadRows();
        } catch (err) {
            toast(err.message);
        }
    }
});

// ---------- Create / edit ----------
$('add-btn').addEventListener('click', () => openEditor(null));
$('editor-cancel').addEventListener('click', () => $('editor').close());

function openEditor(row) {
    const res = RESOURCES[state.resource];
    state.editingId = row?.id ?? null;
    $('editor-title').textContent = row ? `Edit ${res.singular}` : `New ${res.singular}`;
    $('editor-error').hidden = true;

    $('editor-fields').innerHTML = [...COMMON_FIELDS, ...res.fields].map((f) => {
        const isPassword = f.key === 'password';
        const required = f.required || (isPassword && !row);
        const hint = isPassword && row ? ' <span class="hint">(leave blank to keep)</span>' : '';
        const value = isPassword ? '' : esc(row?.[f.key] ?? '');
        return `<label>${esc(f.label)}${hint}
            <input name="${f.key}" type="${f.type || 'text'}" value="${value}" ${required ? 'required' : ''}
                   ${isPassword ? 'minlength="6" autocomplete="new-password"' : ''}>
        </label>`;
    }).join('');
    $('editor').showModal();
}

$('editor-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const data = Object.fromEntries(new FormData(e.target));
    if (!data.password) data.password = null;

    const id = state.editingId;
    try {
        await api(id ? `/api/${state.resource}/${id}` : `/api/${state.resource}`, {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(data),
        });
        $('editor').close();
        toast(`${RESOURCES[state.resource].singular} ${id ? 'updated' : 'created'}`);
        loadRows();
    } catch (err) {
        $('editor-error').textContent = err.message;
        $('editor-error').hidden = false;
    }
});

// ---------- Utils ----------
function esc(value) {
    return String(value).replace(/[&<>"']/g, (c) =>
        ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}

let toastTimer;
function toast(message) {
    $('toast').textContent = message;
    $('toast').hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => ($('toast').hidden = true), 3000);
}

boot();
