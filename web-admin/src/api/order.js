import request from '@/utils/request'

export function getOrderList(params) {
  return request.get('/admin/order/page', { params })
}

export function getOrderDetail(id) {
  return request.get(`/admin/order/${id}`)
}

export function updateOrderStatus(id, status, remark) {
  return request.put(`/admin/order/${id}/status`, { status, remark })
}
