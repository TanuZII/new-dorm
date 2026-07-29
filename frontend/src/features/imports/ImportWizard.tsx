import { type FormEvent, useState } from 'react'
import { CheckCircle2, Download, FileSpreadsheet, RotateCcw, ShieldCheck, Upload, XCircle } from 'lucide-react'
import { api, download } from '../../api/client'

type RowError = { rowNumber: number; field: string; rejectedValue: string | null; code: string; message: string }
type Preview = { token: string; sha256: string; totalRows: number; validRows: number; invalidRows: number; errors: RowError[]; expiresAt: string }
type Confirm = { token: string; importedRows: number }
const xlsxType = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'

export function ImportWizard() {
  const [file, setFile] = useState<File | null>(null)
  const [preview, setPreview] = useState<Preview | null>(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [confirmed, setConfirmed] = useState<number | null>(null)

  function select(next: File | null) {
    setPreview(null); setConfirmed(null); setError('')
    if (!next) return setFile(null)
    if (!next.name.toLowerCase().endsWith('.xlsx') || (next.type && next.type !== xlsxType)) {
      setFile(null); setError('รองรับเฉพาะไฟล์ .xlsx เท่านั้น'); return
    }
    if (next.size > 10 * 1024 * 1024) {
      setFile(null); setError('ไฟล์ต้องมีขนาดไม่เกิน 10 MB'); return
    }
    setFile(next)
  }

  async function inspect(event: FormEvent) {
    event.preventDefault()
    if (!file) return
    setBusy(true); setError('')
    const body = new FormData(); body.append('file', file)
    try { setPreview(await api.post<Preview>('/api/v1/imports/master-data/preview', body)) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'ตรวจสอบไฟล์ไม่สำเร็จ') }
    finally { setBusy(false) }
  }

  async function confirm() {
    if (!preview || preview.invalidRows > 0) return
    setBusy(true); setError('')
    try { setConfirmed((await api.post<Confirm>(`/api/v1/imports/${preview.token}/confirm`)).importedRows) }
    catch (cause) { setError(cause instanceof Error ? cause.message : 'ยืนยันนำเข้าไม่สำเร็จ') }
    finally { setBusy(false) }
  }

  async function downloadErrors() {
    if (!preview) return
    try {
      const blob = await download(`/api/v1/imports/${preview.token}/errors.xlsx`)
      const href = URL.createObjectURL(blob)
      const anchor = document.createElement('a')
      anchor.href = href; anchor.download = 'import-errors.xlsx'; anchor.click()
      URL.revokeObjectURL(href)
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'ดาวน์โหลดไม่สำเร็จ') }
  }

  function reset() { setFile(null); setPreview(null); setConfirmed(null); setError('') }

  return <div className="content admin-page import-page">
    <section className="page-intro"><div><p className="eyebrow">MASTER DATA · XLSX</p><h1>นำเข้าข้อมูล</h1><p>ระบบจะตรวจทุกแถวก่อนบันทึก และไม่บันทึกบางส่วนเมื่อพบข้อผิดพลาด</p></div>{(file || preview) && <button className="secondary-action" onClick={reset}><RotateCcw size={15} /> เริ่มใหม่</button>}</section>
    <ol className="import-steps"><li className={!preview ? 'is-current' : 'is-done'}><i>1</i><span><b>เลือกไฟล์</b><small>สูงสุด 10 MB</small></span></li><li className={preview && confirmed === null ? 'is-current' : preview ? 'is-done' : ''}><i>2</i><span><b>ตรวจสอบ</b><small>ไม่มีการบันทึกข้อมูล</small></span></li><li className={confirmed !== null ? 'is-done' : ''}><i>3</i><span><b>ยืนยัน</b><small>บันทึกทั้งไฟล์</small></span></li></ol>
    {!preview && <form className="panel upload-zone" onSubmit={inspect}><FileSpreadsheet size={38} /><h2>เลือกไฟล์ข้อมูลตั้งต้น</h2><p>หัวตาราง: type, code, nameTh, nameEn, parentId, effectiveFrom, effectiveTo</p><label className="file-picker"><Upload size={16} /> เลือกไฟล์ Excel<input aria-label="ไฟล์ Excel" type="file" accept={`.xlsx,${xlsxType}`} onChange={(event) => select(event.target.files?.[0] || null)} /></label>{file && <div className="selected-file"><b>{file.name}</b><small>{(file.size / 1024).toLocaleString('th-TH', { maximumFractionDigits: 1 })} KB</small></div>}<button className="primary-action" disabled={!file || busy}>{busy ? 'กำลังตรวจสอบ…' : 'ตรวจสอบไฟล์'}</button></form>}
    {preview && <PreviewPanel preview={preview} confirmed={confirmed} busy={busy} confirm={confirm} downloadErrors={downloadErrors} />}
    {error && <p className="import-alert" role="alert">{error}</p>}
  </div>
}

function PreviewPanel({ preview, confirmed, busy, confirm, downloadErrors }: { preview: Preview; confirmed: number | null; busy: boolean; confirm: () => void; downloadErrors: () => void }) {
  if (confirmed !== null) return <section className="preview-stack"><article className="panel import-success" role="status"><CheckCircle2 size={38} /><h2>นำเข้าสำเร็จ {confirmed.toLocaleString('th-TH')} แถว</h2><p>ข้อมูลทั้งหมดถูกบันทึกใน transaction เดียวและมี audit log แล้ว</p></article></section>
  return <section className="preview-stack">
    <div className="preview-metrics">
      <article aria-label={`ทั้งหมด ${preview.totalRows} แถว`}><span>ทั้งหมด</span><b>{preview.totalRows}</b><small>แถว</small></article>
      <article aria-label={`ผ่าน ${preview.validRows} แถว`} className="is-valid"><CheckCircle2 size={18} /><span>ผ่าน</span><b>{preview.validRows}</b><small>แถว</small></article>
      <article aria-label={`ผิดพลาด ${preview.invalidRows} แถว`} className={preview.invalidRows ? 'is-invalid' : ''}>{preview.invalidRows ? <XCircle size={18} /> : <ShieldCheck size={18} />}<span>ผิดพลาด</span><b>{preview.invalidRows}</b><small>แถว</small></article>
    </div>
    <article className="panel hash-panel"><div><span>SHA-256</span><code>{preview.sha256}</code></div><div><span>ยืนยันได้ถึง</span><b>{new Intl.DateTimeFormat('th-TH', { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(preview.expiresAt))}</b></div></article>
    {preview.errors.length > 0 && <article className="data-panel"><div className="data-row data-row--header import-error-row"><span>แถว</span><span>ฟิลด์</span><span>ค่าที่ไม่ผ่าน</span><span>สาเหตุ</span></div>{preview.errors.map((item, index) => <div className="data-row import-error-row" key={`${item.rowNumber}-${item.field}-${index}`}><span data-label="แถว"><b>{item.rowNumber}</b></span><span data-label="ฟิลด์"><code>{item.field}</code></span><span data-label="ค่าที่ไม่ผ่าน">{item.rejectedValue || '—'}</span><span data-label="สาเหตุ"><b>{item.message}</b><small>{item.code}</small></span></div>)}</article>}
    <div className="import-actions">{preview.invalidRows > 0 ? <><p>แก้ไขทุกแถวที่ผิดแล้วนำไฟล์มาตรวจใหม่ ระบบยังไม่ได้บันทึกข้อมูล</p><button className="secondary-action" onClick={downloadErrors}><Download size={16} /> ดาวน์โหลดรายการข้อผิดพลาด</button></> : <><p>ไฟล์ผ่านการตรวจสอบทุกแถว พร้อมบันทึกข้อมูลแบบ atomic</p><button className="primary-action" disabled={busy} onClick={confirm}>{busy ? 'กำลังนำเข้า…' : 'ยืนยันนำเข้าข้อมูล'}</button></>}</div>
  </section>
}
