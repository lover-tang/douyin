/**
 * 抖音无货源发货工具 - 前端逻辑
 */

const API_BASE = '/api';

// ===== 全局状态 =====
let currentPage = 'dashboard';
let ordersPagination = { page: 0, size: 20, totalPages: 0 };
let currentSearchKeyword = '';
let currentStatusFilter = null;

// ===== 初始化 =====
document.addEventListener('DOMContentLoaded', () => {
    navigateTo('dashboard');
    loadDashboardStats();
});

// ===== 页面导航 =====
function navigateTo(page) {
    currentPage = page;

    // 更新导航选中
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.toggle('active', item.dataset.page === page);
    });

    // 切换内容区域
    document.querySelectorAll('.page-section').forEach(section => {
        section.classList.toggle('active', section.id === 'page-' + page);
    });

    // 更新标题
    const titles = {
        'dashboard': '工作台',
        'orders': '订单管理',
        'label-convert': '面单转换',
        'tracking-sync': '轨迹同步',
        'logistics-mapping': '物流映射'
    };
    document.getElementById('page-title').textContent = titles[page] || '';

    // 加载页面数据
    switch (page) {
        case 'dashboard':
            loadDashboardStats();
            break;
        case 'orders':
            loadOrders();
            break;
        case 'label-convert':
            loadLabelConvertData();
            break;
        case 'tracking-sync':
            loadTrackingSyncData();
            break;
        case 'logistics-mapping':
            loadLogisticsMappings();
            break;
    }
}

// ===== API 请求封装 =====
async function apiRequest(url, method = 'GET', data = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' },
    };
    if (data) {
        options.body = JSON.stringify(data);
    }
    try {
        const response = await fetch(API_BASE + url, options);
        const result = await response.json();
        if (result.code !== 200) {
            throw new Error(result.message || '请求失败');
        }
        return result.data;
    } catch (error) {
        showToast(error.message, 'error');
        throw error;
    }
}

// ===== 仪表盘 =====
async function loadDashboardStats() {
    try {
        const stats = await apiRequest('/orders/dashboard');
        document.getElementById('stat-total').textContent = stats.totalOrders || 0;
        document.getElementById('stat-pending').textContent = stats.pendingOrders || 0;
        document.getElementById('stat-shipped').textContent = stats.shippedOrders + stats.syncingOrders || 0;
        document.getElementById('stat-completed').textContent = stats.completedOrders || 0;
    } catch (e) {
        // 静默失败
    }
}

// ===== 订单管理 =====
async function loadOrders() {
    try {
        let url = `/orders?page=${ordersPagination.page}&size=${ordersPagination.size}`;
        if (currentSearchKeyword) url += `&keyword=${encodeURIComponent(currentSearchKeyword)}`;
        if (currentStatusFilter !== null && currentStatusFilter !== '') url += `&status=${currentStatusFilter}`;

        const pageData = await apiRequest(url);
        renderOrdersTable(pageData.content || []);
        ordersPagination.totalPages = pageData.totalPages || 0;
        renderPagination(pageData);
    } catch (e) {
        // 错误已在 apiRequest 中处理
    }
}

function renderOrdersTable(orders) {
    const tbody = document.getElementById('orders-tbody');
    if (!orders || orders.length === 0) {
        tbody.innerHTML = `
            <tr><td colspan="8" class="empty-state">
                <i class="fas fa-inbox"></i>
                <p>暂无订单数据</p>
                <button class="btn btn-primary" onclick="showAddOrderModal()">
                    <i class="fas fa-plus"></i> 添加订单
                </button>
            </td></tr>`;
        return;
    }

    tbody.innerHTML = orders.map(order => `
        <tr>
            <td><strong>${escapeHtml(order.douyinOrderNo)}</strong></td>
            <td>${escapeHtml(order.productName || '-')}</td>
            <td>
                <div>${escapeHtml(order.receiverName)}</div>
                <div style="font-size:12px;color:var(--text-secondary)">${escapeHtml(order.receiverPhone)}</div>
            </td>
            <td>${escapeHtml(order.taobaoTrackingNo || '-')}</td>
            <td>${escapeHtml(order.douyinTrackingNo || '-')}</td>
            <td>${getStatusTag(order.status)}</td>
            <td>${order.labelConverted ? '<span class="status-tag status-synced">已转换</span>' : '<span class="status-tag status-unsynced">待转换</span>'}</td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-outline" onclick="showOrderDetail(${order.id})" title="详情">
                        <i class="fas fa-eye"></i>
                    </button>
                    <button class="btn btn-sm btn-outline" onclick="showEditOrderModal(${order.id})" title="编辑">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteOrder(${order.id})" title="删除">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

function renderPagination(pageData) {
    const container = document.getElementById('orders-pagination');
    if (!pageData || pageData.totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = `
        <button ${pageData.first ? 'disabled' : ''} onclick="goToPage(${pageData.number - 1})">
            <i class="fas fa-chevron-left"></i>
        </button>`;

    for (let i = 0; i < pageData.totalPages; i++) {
        if (pageData.totalPages > 7 && Math.abs(i - pageData.number) > 2 && i !== 0 && i !== pageData.totalPages - 1) {
            if (Math.abs(i - pageData.number) === 3) html += '<span class="page-info">...</span>';
            continue;
        }
        html += `<button class="${i === pageData.number ? 'active' : ''}" onclick="goToPage(${i})">${i + 1}</button>`;
    }

    html += `
        <button ${pageData.last ? 'disabled' : ''} onclick="goToPage(${pageData.number + 1})">
            <i class="fas fa-chevron-right"></i>
        </button>
        <span class="page-info">共 ${pageData.totalElements} 条</span>`;

    container.innerHTML = html;
}

function goToPage(page) {
    ordersPagination.page = page;
    loadOrders();
}

function searchOrders() {
    currentSearchKeyword = document.getElementById('search-keyword').value;
    currentStatusFilter = document.getElementById('filter-status').value;
    ordersPagination.page = 0;
    loadOrders();
}

function getStatusTag(status) {
    const map = {
        0: ['待采购', 'status-pending'],
        1: ['已采购', 'status-purchased'],
        2: ['已发货', 'status-shipped'],
        3: ['同步中', 'status-syncing'],
        4: ['已签收', 'status-completed'],
        5: ['异常', 'status-exception']
    };
    const [text, cls] = map[status] || ['未知', 'status-pending'];
    return `<span class="status-tag ${cls}">${text}</span>`;
}

// ===== 订单CRUD =====
function showAddOrderModal() {
    document.getElementById('order-modal-title').textContent = '添加订单';
    document.getElementById('order-form').reset();
    document.getElementById('order-id').value = '';
    showModal('order-modal');
}

async function showEditOrderModal(id) {
    try {
        const order = await apiRequest(`/orders/${id}`);
        document.getElementById('order-modal-title').textContent = '编辑订单';
        document.getElementById('order-id').value = order.id;
        document.getElementById('order-douyinNo').value = order.douyinOrderNo;
        document.getElementById('order-taobaoNo').value = order.taobaoOrderNo || '';
        document.getElementById('order-productName').value = order.productName || '';
        document.getElementById('order-receiverName').value = order.receiverName;
        document.getElementById('order-receiverPhone').value = order.receiverPhone;
        document.getElementById('order-receiverProvince').value = order.receiverProvince || '';
        document.getElementById('order-receiverCity').value = order.receiverCity || '';
        document.getElementById('order-receiverDistrict').value = order.receiverDistrict || '';
        document.getElementById('order-receiverAddress').value = order.receiverAddress;
        document.getElementById('order-remark').value = order.remark || '';
        showModal('order-modal');
    } catch (e) { /* 已处理 */ }
}

async function saveOrder() {
    const id = document.getElementById('order-id').value;
    const data = {
        douyinOrderNo: document.getElementById('order-douyinNo').value,
        taobaoOrderNo: document.getElementById('order-taobaoNo').value,
        productName: document.getElementById('order-productName').value,
        receiverName: document.getElementById('order-receiverName').value,
        receiverPhone: document.getElementById('order-receiverPhone').value,
        receiverProvince: document.getElementById('order-receiverProvince').value,
        receiverCity: document.getElementById('order-receiverCity').value,
        receiverDistrict: document.getElementById('order-receiverDistrict').value,
        receiverAddress: document.getElementById('order-receiverAddress').value,
        remark: document.getElementById('order-remark').value,
    };

    try {
        if (id) {
            await apiRequest(`/orders/${id}`, 'PUT', data);
        } else {
            await apiRequest('/orders', 'POST', data);
        }
        showToast(id ? '订单更新成功' : '订单添加成功', 'success');
        closeModal('order-modal');
        loadOrders();
        loadDashboardStats();
    } catch (e) { /* 已处理 */ }
}

async function deleteOrder(id) {
    if (!confirm('确定删除该订单？')) return;
    try {
        await apiRequest(`/orders/${id}`, 'DELETE');
        showToast('订单删除成功', 'success');
        loadOrders();
        loadDashboardStats();
    } catch (e) { /* 已处理 */ }
}

async function showOrderDetail(id) {
    try {
        const order = await apiRequest(`/orders/${id}`);
        const tracking = await apiRequest(`/tracking/order/${id}`);

        let html = `
            <div style="margin-bottom:20px;">
                <div class="form-row" style="margin-bottom:12px;">
                    <div><strong>抖音订单号:</strong> ${escapeHtml(order.douyinOrderNo)}</div>
                    <div><strong>淘宝订单号:</strong> ${escapeHtml(order.taobaoOrderNo || '-')}</div>
                </div>
                <div class="form-row" style="margin-bottom:12px;">
                    <div><strong>商品:</strong> ${escapeHtml(order.productName || '-')}</div>
                    <div><strong>状态:</strong> ${getStatusTag(order.status)}</div>
                </div>
                <div class="form-row" style="margin-bottom:12px;">
                    <div><strong>收件人:</strong> ${escapeHtml(order.receiverName)} ${escapeHtml(order.receiverPhone)}</div>
                    <div><strong>地址:</strong> ${escapeHtml((order.receiverProvince || '') + (order.receiverCity || '') + (order.receiverDistrict || '') + order.receiverAddress)}</div>
                </div>
                <div class="form-row" style="margin-bottom:12px;">
                    <div><strong>淘宝物流:</strong> ${escapeHtml(order.taobaoLogisticsCompany || '-')} ${escapeHtml(order.taobaoTrackingNo || '')}</div>
                    <div><strong>抖音物流:</strong> ${escapeHtml(order.douyinLogisticsCompany || '-')} ${escapeHtml(order.douyinTrackingNo || '')}</div>
                </div>
            </div>
            <h4 style="margin-bottom:16px;">物流轨迹</h4>`;

        if (tracking && tracking.length > 0) {
            html += '<div class="timeline">';
            tracking.forEach(t => {
                html += `
                    <div class="timeline-item">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <div class="timeline-time">${formatDateTime(t.trackingTime)}</div>
                            <div class="timeline-desc">${escapeHtml(t.description)}</div>
                            ${t.location ? `<div class="timeline-location"><i class="fas fa-map-marker-alt"></i> ${escapeHtml(t.location)}</div>` : ''}
                            <div class="timeline-sync-status">
                                ${t.synced ? '<span class="status-tag status-synced">已同步</span>' : '<span class="status-tag status-unsynced">待同步</span>'}
                            </div>
                        </div>
                    </div>`;
            });
            html += '</div>';
        } else {
            html += '<p style="color:var(--text-secondary);text-align:center;padding:20px;">暂无轨迹信息</p>';
        }

        document.getElementById('detail-modal-body').innerHTML = html;
        showModal('detail-modal');
    } catch (e) { /* 已处理 */ }
}

// ===== 面单转换 =====
async function loadLabelConvertData() {
    try {
        // 加载待转换订单
        const pageData = await apiRequest('/orders?page=0&size=100');
        const orders = (pageData.content || []).filter(o => !o.labelConverted);
        renderConvertOrderSelect(orders);

        // 加载物流映射
        const mappings = await apiRequest('/labels/logistics-mappings');
        renderLogisticsSelect(mappings);

        // 加载转换记录
        const conversions = await apiRequest('/labels');
        renderConversionRecords(conversions || []);
    } catch (e) { /* 已处理 */ }
}

function renderConvertOrderSelect(orders) {
    const select = document.getElementById('convert-orderId');
    select.innerHTML = '<option value="">请选择订单</option>' +
        orders.map(o => `<option value="${o.id}">${o.douyinOrderNo} - ${o.receiverName} - ${o.productName || '无商品名'}</option>`).join('');
}

function renderLogisticsSelect(mappings) {
    const select = document.getElementById('convert-taobaoLogistics');
    select.innerHTML = '<option value="">请选择物流公司</option>' +
        mappings.map(m => `<option value="${m.taobaoName}">${m.taobaoName}</option>`).join('');
}

function renderConversionRecords(conversions) {
    const tbody = document.getElementById('conversions-tbody');
    if (!conversions || conversions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-secondary);padding:30px;">暂无转换记录</td></tr>';
        return;
    }

    tbody.innerHTML = conversions.map(c => `
        <tr>
            <td><strong>${escapeHtml(c.douyinOrderNo)}</strong></td>
            <td>${escapeHtml(c.taobaoLogisticsCompany || '-')}</td>
            <td>${escapeHtml(c.taobaoTrackingNo || '-')}</td>
            <td>${escapeHtml(c.douyinLogisticsCompany || '-')}</td>
            <td>${escapeHtml(c.douyinTrackingNo || '-')}</td>
            <td>${c.status === 1 ? '<span class="status-tag status-synced">成功</span>' : '<span class="status-tag status-exception">失败</span>'}</td>
            <td>
                <button class="btn btn-sm btn-info" onclick="previewLabel(${c.orderId})">
                    <i class="fas fa-eye"></i> 预览
                </button>
            </td>
        </tr>
    `).join('');
}

async function submitLabelConvert() {
    const data = {
        orderId: parseInt(document.getElementById('convert-orderId').value),
        taobaoLogisticsCompany: document.getElementById('convert-taobaoLogistics').value,
        taobaoTrackingNo: document.getElementById('convert-taobaoTrackingNo').value,
        senderName: document.getElementById('convert-senderName').value,
        senderPhone: document.getElementById('convert-senderPhone').value,
        senderAddress: document.getElementById('convert-senderAddress').value,
    };

    if (!data.orderId || !data.taobaoLogisticsCompany || !data.taobaoTrackingNo) {
        showToast('请填写完整信息', 'warning');
        return;
    }

    try {
        const result = await apiRequest('/labels/convert', 'POST', data);
        showToast('面单转换成功！', 'success');
        loadLabelConvertData();
        showLabelPreview(result);
    } catch (e) { /* 已处理 */ }
}

async function previewLabel(orderId) {
    try {
        const conversion = await apiRequest(`/labels/order/${orderId}`);
        showLabelPreview(conversion);
    } catch (e) { /* 已处理 */ }
}

function showLabelPreview(conversion) {
    const html = `
        <div class="label-preview">
            <div class="label-preview-header">
                <h4>${escapeHtml(conversion.douyinLogisticsCompany || '快递')}</h4>
            </div>
            <div class="label-info-grid">
                <div class="label-info-section">
                    <h5>发件人信息</h5>
                    <p>
                        ${escapeHtml(conversion.senderName || '-')}<br>
                        ${escapeHtml(conversion.senderPhone || '-')}<br>
                        ${escapeHtml(conversion.senderAddress || '-')}
                    </p>
                </div>
                <div class="label-info-section">
                    <h5>收件人信息</h5>
                    <p>
                        ${escapeHtml(conversion.receiverName || '-')}<br>
                        ${escapeHtml(conversion.receiverPhone || '-')}<br>
                        ${escapeHtml(conversion.receiverAddress || '-')}
                    </p>
                </div>
            </div>
            <div class="label-barcode">
                ${escapeHtml(conversion.douyinTrackingNo || '-')}
            </div>
            <div style="text-align:center;margin-top:12px;font-size:12px;color:var(--text-secondary);">
                抖音订单号: ${escapeHtml(conversion.douyinOrderNo || '-')}
            </div>
        </div>`;

    document.getElementById('label-preview-container').innerHTML = html;
    showModal('label-preview-modal');
}

// ===== 轨迹同步 =====
async function loadTrackingSyncData() {
    try {
        // 加载有轨迹同步的订单
        const pageData = await apiRequest('/orders?page=0&size=100');
        const orders = (pageData.content || []).filter(o => o.labelConverted);
        renderTrackingOrderSelect(orders);
        renderTrackingSyncTable(orders);
    } catch (e) { /* 已处理 */ }
}

function renderTrackingOrderSelect(orders) {
    const select = document.getElementById('tracking-orderId');
    if (!select) return;
    select.innerHTML = '<option value="">请选择订单</option>' +
        orders.map(o => `<option value="${o.id}">${o.douyinOrderNo} - ${o.taobaoTrackingNo || '无单号'}</option>`).join('');
}

function renderTrackingSyncTable(orders) {
    const tbody = document.getElementById('tracking-sync-tbody');
    if (!orders || orders.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" style="text-align:center;color:var(--text-secondary);padding:30px;">暂无需要同步的订单，请先进行面单转换</td></tr>';
        return;
    }

    tbody.innerHTML = orders.map(o => `
        <tr>
            <td><strong>${escapeHtml(o.douyinOrderNo)}</strong></td>
            <td>${escapeHtml(o.taobaoLogisticsCompany || '-')}</td>
            <td>${escapeHtml(o.taobaoTrackingNo || '-')}</td>
            <td>${getStatusTag(o.status)}</td>
            <td>${o.trackingSyncEnabled ? '<span class="status-tag status-synced">已开启</span>' : '<span class="status-tag status-unsynced">未开启</span>'}</td>
            <td>${o.lastSyncTime ? formatDateTime(o.lastSyncTime) : '-'}</td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm btn-info" onclick="viewTracking(${o.id})" title="查看轨迹">
                        <i class="fas fa-route"></i>
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="showAddTrackingModal(${o.id})" title="添加轨迹">
                        <i class="fas fa-plus"></i>
                    </button>
                    <button class="btn btn-sm btn-success" onclick="syncOrderTracking(${o.id})" title="同步轨迹">
                        <i class="fas fa-sync"></i>
                    </button>
                </div>
            </td>
        </tr>
    `).join('');
}

async function viewTracking(orderId) {
    try {
        const tracking = await apiRequest(`/tracking/order/${orderId}`);
        let html = '';

        if (tracking && tracking.length > 0) {
            html = '<div class="timeline">';
            tracking.forEach(t => {
                html += `
                    <div class="timeline-item">
                        <div class="timeline-dot"></div>
                        <div class="timeline-content">
                            <div class="timeline-time">${formatDateTime(t.trackingTime)}</div>
                            <div class="timeline-desc">${escapeHtml(t.description)}</div>
                            ${t.location ? `<div class="timeline-location"><i class="fas fa-map-marker-alt"></i> ${escapeHtml(t.location)}</div>` : ''}
                            <div class="timeline-sync-status">
                                ${t.synced
                                    ? '<span class="status-tag status-synced">已同步到抖音</span>'
                                    : '<span class="status-tag status-unsynced">待同步</span>'}
                                ${t.syncTime ? `<span style="font-size:11px;color:var(--text-light);margin-left:8px;">同步时间: ${formatDateTime(t.syncTime)}</span>` : ''}
                            </div>
                        </div>
                    </div>`;
            });
            html += '</div>';
        } else {
            html = '<div class="empty-state"><i class="fas fa-route"></i><p>暂无轨迹信息</p></div>';
        }

        document.getElementById('tracking-detail-body').innerHTML = html;
        showModal('tracking-detail-modal');
    } catch (e) { /* 已处理 */ }
}

let addTrackingOrderId = null;

function showAddTrackingModal(orderId) {
    addTrackingOrderId = orderId;
    document.getElementById('tracking-items-container').innerHTML = '';
    addTrackingItem();
    showModal('add-tracking-modal');
}

function addTrackingItem() {
    const container = document.getElementById('tracking-items-container');
    const idx = container.children.length;
    const now = new Date();
    const defaultTime = now.getFullYear() + '-' +
        String(now.getMonth() + 1).padStart(2, '0') + '-' +
        String(now.getDate()).padStart(2, '0') + ' ' +
        String(now.getHours()).padStart(2, '0') + ':' +
        String(now.getMinutes()).padStart(2, '0') + ':' +
        String(now.getSeconds()).padStart(2, '0');

    const div = document.createElement('div');
    div.className = 'card';
    div.style.marginBottom = '12px';
    div.innerHTML = `
        <div class="card-body" style="padding:16px;">
            <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
                <strong>轨迹 #${idx + 1}</strong>
                <button class="btn btn-sm btn-danger" onclick="this.closest('.card').remove()">
                    <i class="fas fa-times"></i>
                </button>
            </div>
            <div class="form-row">
                <div class="form-group" style="margin-bottom:8px;">
                    <label class="form-label">时间</label>
                    <input type="text" class="form-control tracking-time" value="${defaultTime}" placeholder="yyyy-MM-dd HH:mm:ss">
                </div>
                <div class="form-group" style="margin-bottom:8px;">
                    <label class="form-label">状态</label>
                    <select class="form-control tracking-status">
                        <option value="COLLECTED">已揽收</option>
                        <option value="IN_TRANSIT" selected>运输中</option>
                        <option value="DELIVERING">派送中</option>
                        <option value="SIGNED">已签收</option>
                        <option value="EXCEPTION">异常</option>
                    </select>
                </div>
            </div>
            <div class="form-group" style="margin-bottom:8px;">
                <label class="form-label">轨迹描述</label>
                <input type="text" class="form-control tracking-desc" placeholder="例如：快件已到达广州转运中心">
            </div>
            <div class="form-group" style="margin-bottom:0;">
                <label class="form-label">位置（可选）</label>
                <input type="text" class="form-control tracking-location" placeholder="例如：广州市白云区">
            </div>
        </div>`;
    container.appendChild(div);
}

async function submitTracking() {
    const items = [];
    document.querySelectorAll('#tracking-items-container .card').forEach(card => {
        items.push({
            trackingTime: card.querySelector('.tracking-time').value,
            description: card.querySelector('.tracking-desc').value,
            location: card.querySelector('.tracking-location').value,
            trackingStatus: card.querySelector('.tracking-status').value,
        });
    });

    if (items.length === 0 || !items[0].description) {
        showToast('请至少添加一条轨迹', 'warning');
        return;
    }

    try {
        await apiRequest('/tracking', 'POST', {
            orderId: addTrackingOrderId,
            trackingItems: items,
        });
        showToast('轨迹添加成功', 'success');
        closeModal('add-tracking-modal');
        loadTrackingSyncData();
    } catch (e) { /* 已处理 */ }
}

async function syncOrderTracking(orderId) {
    try {
        const result = await apiRequest(`/tracking/sync/${orderId}`, 'POST');
        showToast(`同步成功，共同步 ${result.syncedCount} 条轨迹`, 'success');
        loadTrackingSyncData();
    } catch (e) { /* 已处理 */ }
}

async function syncAllTracking() {
    try {
        const result = await apiRequest('/tracking/sync/all', 'POST');
        showToast(`批量同步完成，共同步 ${result.syncedCount} 条轨迹`, 'success');
        loadTrackingSyncData();
    } catch (e) { /* 已处理 */ }
}

// ===== 物流映射管理 =====
async function loadLogisticsMappings() {
    try {
        const mappings = await apiRequest('/labels/logistics-mappings');
        renderMappingsTable(mappings || []);
    } catch (e) { /* 已处理 */ }
}

function renderMappingsTable(mappings) {
    const tbody = document.getElementById('mappings-tbody');
    if (!mappings || mappings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;color:var(--text-secondary);padding:30px;">暂无映射数据</td></tr>';
        return;
    }

    tbody.innerHTML = mappings.map(m => `
        <tr>
            <td>${escapeHtml(m.taobaoName)}</td>
            <td>${escapeHtml(m.taobaoCode || '-')}</td>
            <td>${escapeHtml(m.douyinName)}</td>
            <td>${escapeHtml(m.douyinCode || '-')}</td>
            <td>
                <button class="btn btn-sm btn-danger" onclick="deleteMapping(${m.id})">
                    <i class="fas fa-trash"></i>
                </button>
            </td>
        </tr>
    `).join('');
}

function showAddMappingModal() {
    document.getElementById('mapping-form').reset();
    showModal('mapping-modal');
}

async function saveMapping() {
    const data = {
        taobaoName: document.getElementById('mapping-taobaoName').value,
        taobaoCode: document.getElementById('mapping-taobaoCode').value,
        douyinName: document.getElementById('mapping-douyinName').value,
        douyinCode: document.getElementById('mapping-douyinCode').value,
        enabled: true,
    };

    if (!data.taobaoName || !data.douyinName) {
        showToast('请填写必要信息', 'warning');
        return;
    }

    try {
        await apiRequest('/labels/logistics-mappings', 'POST', data);
        showToast('添加成功', 'success');
        closeModal('mapping-modal');
        loadLogisticsMappings();
    } catch (e) { /* 已处理 */ }
}

async function deleteMapping(id) {
    if (!confirm('确定删除该映射？')) return;
    try {
        await apiRequest(`/labels/logistics-mappings/${id}`, 'DELETE');
        showToast('删除成功', 'success');
        loadLogisticsMappings();
    } catch (e) { /* 已处理 */ }
}

// ===== 模态框管理 =====
function showModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

// 点击遮罩关闭
document.addEventListener('click', (e) => {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.classList.remove('active');
    }
});

// ===== 工具函数 =====
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    if (Array.isArray(dateStr)) {
        // Java LocalDateTime 序列化为数组格式
        const [y, m, d, h, mi, s] = dateStr;
        return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')} ${String(h).padStart(2, '0')}:${String(mi).padStart(2, '0')}:${String(s || 0).padStart(2, '0')}`;
    }
    return dateStr.replace('T', ' ').substring(0, 19);
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const icons = {
        success: 'fas fa-check-circle',
        error: 'fas fa-times-circle',
        warning: 'fas fa-exclamation-triangle',
        info: 'fas fa-info-circle',
    };
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerHTML = `<i class="${icons[type]}"></i> ${escapeHtml(message)}`;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}
