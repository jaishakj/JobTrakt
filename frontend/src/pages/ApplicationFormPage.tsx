import { useEffect, useState, type FormEvent } from 'react'
import { request, jsonBody } from '../api/client'
import { useRouter } from '../lib/router'
import { useResource } from '../lib/useResource'
import type { Application, ApplicationStatus, EmploymentType } from '../types'
import { employmentLabels, statusLabels, toLocalInput } from '../lib/format'
import { ErrorState, Loading, PageTitle } from '../components/Feedback'

type FormValues = { company: string; position: string; location: string; employmentType: EmploymentType; salary: string; status: ApplicationStatus; appliedDate: string; interviewDate: string; followUpDate: string; jobUrl: string; notes: string }
const initial: FormValues = { company: '', position: '', location: '', employmentType: 'FULL_TIME', salary: '', status: 'APPLIED', appliedDate: new Date().toISOString().slice(0, 10), interviewDate: '', followUpDate: '', jobUrl: '', notes: '' }
const statuses: ApplicationStatus[] = ['SAVED', 'APPLIED', 'SCREENING', 'INTERVIEW', 'OFFER', 'REJECTED', 'WITHDRAWN']
const employment: EmploymentType[] = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE']

export function ApplicationFormPage({ id }: { id?: string }) {
  const editing = Boolean(id)
  const { navigate } = useRouter()
  const [values, setValues] = useState<FormValues>(initial)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const detail = useResource(() => id ? request<{ application: Application }>(`/applications/${id}`) : Promise.resolve(null), [id])
  useEffect(() => {
    if (!detail.data?.application) return
    const app = detail.data.application
    setValues({ company: app.company, position: app.position, location: app.location ?? '', employmentType: app.employmentType,
      salary: app.salary == null ? '' : String(app.salary), status: app.status, appliedDate: app.appliedDate,
      interviewDate: toLocalInput(app.interviewDate), followUpDate: toLocalInput(app.followUpDate), jobUrl: app.jobUrl ?? '', notes: app.notes ?? '' })
  }, [detail.data])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setSaving(true); setError('')
    const body = { ...values, salary: values.salary === '' ? null : Number(values.salary), interviewDate: values.interviewDate ? new Date(values.interviewDate).toISOString() : null, followUpDate: values.followUpDate ? new Date(values.followUpDate).toISOString() : null }
    try {
      const app = await request<Application>(editing ? `/applications/${id}` : '/applications', { method: editing ? 'PUT' : 'POST', body: jsonBody(body) })
      navigate(`/applications/${app.id}`)
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'Please check the form and try again.') }
    finally { setSaving(false) }
  }

  function change<K extends keyof FormValues>(key: K, value: FormValues[K]) { setValues(old => ({ ...old, [key]: value })) }
  if (editing && detail.loading) return <div className="page-wrap"><Loading label="Opening the application…" /></div>
  if (editing && (detail.error || !detail.data)) return <div className="page-wrap"><ErrorState message={detail.error ?? 'Application not found.'} onRetry={() => void detail.reload()} /></div>
  return <div className="page-wrap form-page">
    <PageTitle eyebrow={editing ? 'REFINE THE DETAILS' : 'ADD TO YOUR SEARCH'} title={editing ? 'Edit application' : 'New application'} detail="Just enough detail to keep the next step close." />
    <form onSubmit={submit} className="application-form">
      <div className="form-section"><div className="form-section-title"><span>01</span><div><h2>The role</h2><p>Start with the details you’ll want at a glance.</p></div></div><div className="field-grid">
        <label>Company<input required maxLength={160} value={values.company} onChange={e => change('company', e.target.value)} placeholder="e.g. Northstar Studio" /></label>
        <label>Position<input required maxLength={160} value={values.position} onChange={e => change('position', e.target.value)} placeholder="e.g. Product Designer" /></label>
        <label>Location<input maxLength={160} value={values.location} onChange={e => change('location', e.target.value)} placeholder="City, remote, or hybrid" /></label>
        <label>Employment type<select value={values.employmentType} onChange={e => change('employmentType', e.target.value as EmploymentType)}>{employment.map(item => <option key={item} value={item}>{employmentLabels[item]}</option>)}</select></label>
        <label>Salary / annual range<input type="number" min="0" step="0.01" value={values.salary} onChange={e => change('salary', e.target.value)} placeholder="Optional" /></label>
        <label>Application status<select value={values.status} onChange={e => change('status', e.target.value as ApplicationStatus)}>{statuses.map(item => <option key={item} value={item}>{statusLabels[item]}</option>)}</select></label>
      </div></div>
      <div className="form-section"><div className="form-section-title"><span>02</span><div><h2>Dates & next steps</h2><p>JobTrakt can quietly remind you when the moment comes.</p></div></div><div className="field-grid">
        <label>Date applied<input required type="date" value={values.appliedDate} onChange={e => change('appliedDate', e.target.value)} /></label>
        <label>Interview date {values.status === 'INTERVIEW' && <b className="required-mark">required</b>}<input type="datetime-local" required={values.status === 'INTERVIEW'} value={values.interviewDate} onChange={e => change('interviewDate', e.target.value)} /></label>
        <label>Follow-up reminder<input type="datetime-local" value={values.followUpDate} onChange={e => change('followUpDate', e.target.value)} /></label>
        <label>Job posting URL<input type="url" maxLength={2048} value={values.jobUrl} onChange={e => change('jobUrl', e.target.value)} placeholder="https://…" /></label>
        <label className="span-two">Notes<textarea rows={4} maxLength={10000} value={values.notes} onChange={e => change('notes', e.target.value)} placeholder="A detail you’ll be glad you remembered…" /></label>
      </div></div>
      {error && <div className="form-error" role="alert">{error}</div>}
      <div className="form-actions"><button className="button quiet-button" type="button" onClick={() => navigate(editing ? `/applications/${id}` : '/applications')}>Cancel</button><button className="button primary-button" disabled={saving}>{saving ? 'Saving…' : editing ? 'Save changes' : 'Add application'} <span>↗</span></button></div>
    </form>
  </div>
}
