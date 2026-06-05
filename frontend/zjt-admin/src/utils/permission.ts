import { useUserStore } from '@/stores/user'

export function hasPermission(permission: string): boolean {
  const userStore = useUserStore()
  const permissions = userStore.permissions
  return permissions.includes(permission)
}

export function hasRole(role: string): boolean {
  const userStore = useUserStore()
  return userStore.userInfo?.role === role
}

export function hasAnyPermission(permissions: string[]): boolean {
  return permissions.some((p) => hasPermission(p))
}

export function hasAllPermissions(permissions: string[]): boolean {
  return permissions.every((p) => hasPermission(p))
}
