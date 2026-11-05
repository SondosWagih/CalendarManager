package app.calendarmanager.data.local.entity

/** Lecture and Section are independent event types. */
enum class StudyEventType { LECTURE, SECTION }

/** Physical and Online are independent attendance formats; both types may use either. */
enum class AttendanceFormat { PHYSICAL, ONLINE }

enum class ExamKind { MIDTERM, FINAL }

enum class Priority { LOW, MEDIUM, HIGH }

enum class WorkEventKind { WEEKLY, ONE_TIME, UNEXPECTED }

enum class NotificationOwnerType { STUDY_EVENT, EXAM, ASSIGNMENT, WORK_EVENT, TASK }

enum class NotificationKind { ADVANCE_48H, ADVANCE_10M, AT_TIME, OVERDUE_DAILY }

enum class NotificationStatus { SCHEDULED, FIRED, CANCELLED, BLOCKED_NO_PERMISSION }
