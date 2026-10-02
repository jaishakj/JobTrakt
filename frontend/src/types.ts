export type User = { id: number; name: string; email: string; createdAt: string }
export type AuthResponse = { accessToken: string; tokenType: 'Bearer'; expiresIn: number; user: User }
export type ApplicationStatus = 'SAVED' | 'APPLIED' | 'SCREENING' | 'INTERVIEW' | 'OFFER' | 'REJECTED' | 'WITHDRAWN'
export type EmploymentType = 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'INTERNSHIP' | 'FREELANCE'
export type Application = {
  id: number; company: string; position: string; location?: string; employmentType: EmploymentType
  salary?: number; status: ApplicationStatus; appliedDate: string; interviewDate?: string
  followUpDate?: string; jobUrl?: string; notes?: string; createdAt: string; updatedAt: string
}
export type TimelineItem = { id: number; action: string; details: string; createdAt: string }
export type Job = { id: number; applicationId?: number; company?: string; position?: string; type: string; status: string
  priority: string; attempts: number; maxAttempts: number; scheduledAt: string; startedAt?: string
  completedAt?: string; errorMessage?: string; createdAt: string }
export type Notification = { id: number; applicationId?: number; type: string; title: string; message: string; read: boolean; createdAt: string }
export type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
export type NotificationPage = { notifications: Page<Notification>; unreadCount: number }
export type ApplicationDetail = { application: Application; timeline: TimelineItem[]; jobs: Job[]; notifications: Notification[] }
export type Dashboard = {
  totalApplications: number; statusCounts: Record<ApplicationStatus, number>; applicationsThisWeek: number
  applicationsThisMonth: number; responseRate: number; interviewRate: number; offerRate: number
  upcomingInterviews: Application[]; recentApplications: Application[]; unreadNotifications: number
  jobQueue: Record<string, number>; recentNotifications: Notification[]
}
export type ApiError = { status: number; error: string; message: string; path: string }
