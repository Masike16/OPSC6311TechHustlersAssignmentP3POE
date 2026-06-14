const pptxgen = require('C:/Users/masik/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/.pnpm/pptxgenjs@4.0.1/node_modules/pptxgenjs');
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const outDir = 'C:/Users/masik/Downloads';
const outPath = path.join(outDir, 'MediCloud_EMR_Project_Presentation.pptx');

const pptx = new pptxgen();
pptx.layout = 'LAYOUT_WIDE';
pptx.author = 'Tech Hustlers';
pptx.subject = 'MediCloud EMR System project presentation';
pptx.title = 'MediCloud EMR System';
pptx.company = 'Tech Hustlers';
pptx.lang = 'en-ZA';
pptx.theme = {
  headFontFace: 'Aptos Display',
  bodyFontFace: 'Aptos',
  lang: 'en-US'
};
pptx.defineLayout({ name: 'CUSTOM_WIDE', width: 13.333, height: 7.5 });
pptx.layout = 'CUSTOM_WIDE';
pptx.margin = 0;
pptx.defineSlideMaster({
  title: 'BASE',
  background: { color: 'F7FAFC' },
  objects: [
    { line: { x: 0.45, y: 7.05, w: 12.45, h: 0, line: { color: 'D7E1E8', width: 1 } } },
    { text: { text: 'MediCloud EMR | Tech Hustlers', options: { x: 0.55, y: 7.12, w: 4.4, h: 0.18, fontFace: 'Aptos', fontSize: 6.8, color: '60727F', margin: 0 } } }
  ],
  slideNumber: { x: 12.3, y: 7.1, color: '60727F', fontFace: 'Aptos', fontSize: 7 }
});

const C = {
  navy: '102A43', blue: '1F5EFF', teal: '0EAD9B', mint: 'DAF5EF', coral: 'FF6B4A',
  amber: 'F9B115', ink: '243B53', muted: '627D98', pale: 'F7FAFC', line: 'D9E2EC', white: 'FFFFFF', red: 'D64545', green: '2F9E44'
};

function slide(title, kicker) {
  const s = pptx.addSlide('BASE');
  s.background = { color: C.pale };
  if (kicker) s.addText(kicker.toUpperCase(), { x: 0.64, y: 0.32, w: 5.6, h: 0.22, fontSize: 7.5, bold: true, color: C.teal, charSpace: 1.4, margin: 0 });
  if (title) s.addText(title, { x: 0.62, y: 0.58, w: 8.9, h: 0.46, fontSize: 23, bold: true, color: C.navy, margin: 0, breakLine: false, fit: 'shrink' });
  s.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 0.16, h: 7.5, fill: { color: C.teal }, line: { color: C.teal } });
  return s;
}
function pill(s, text, x, y, w, color = C.teal, txt = C.white) {
  s.addShape(pptx.ShapeType.roundRect, { x, y, w, h: 0.3, rectRadius: 0.07, fill: { color }, line: { color, transparency: 100 } });
  s.addText(text, { x: x + 0.08, y: y + 0.075, w: w - 0.16, h: 0.11, fontSize: 6.6, bold: true, color: txt, align: 'center', margin: 0, fit: 'shrink' });
}
function card(s, x, y, w, h, title, body, accent = C.teal) {
  s.addShape(pptx.ShapeType.roundRect, { x, y, w, h, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line, width: 1 } });
  s.addShape(pptx.ShapeType.rect, { x, y, w: 0.08, h, fill: { color: accent }, line: { color: accent } });
  s.addText(title, { x: x + 0.22, y: y + 0.16, w: w - 0.42, h: 0.2, fontSize: 10.4, bold: true, color: C.navy, margin: 0, fit: 'shrink' });
  s.addText(body, { x: x + 0.22, y: y + 0.47, w: w - 0.42, h: h - 0.58, fontSize: 8.1, color: C.ink, breakLine: false, fit: 'shrink', valign: 'top', margin: 0.02, paraSpaceAfterPt: 4 });
}
function miniMetric(s, value, label, x, y, w, color = C.blue) {
  s.addText(value, { x, y, w, h: 0.42, fontSize: 23, bold: true, color, margin: 0, align: 'center', fit: 'shrink' });
  s.addText(label, { x, y: y + 0.45, w, h: 0.28, fontSize: 7.4, bold: true, color: C.muted, align: 'center', margin: 0, fit: 'shrink' });
}
function bulletList(s, items, x, y, w, h, opts = {}) {
  const symbol = opts.symbol || '-';
  const text = items.map(item => `${symbol} ${item}`).join('\n');
  s.addText(text, { x, y, w, h, fontSize: opts.fontSize || 10, color: opts.color || C.ink, breakLine: false, fit: 'shrink', valign: 'mid', margin: 0.02, paraSpaceAfterPt: opts.space || 6 });
}
function table(s, rows, x, y, colWs, rowH, opts = {}) {
  rows.forEach((row, r) => {
    let cx = x;
    row.forEach((cell, c) => {
      const fill = r === 0 ? (opts.header || C.navy) : (r % 2 ? C.white : 'F3F7FA');
      const color = r === 0 ? C.white : C.ink;
      s.addShape(pptx.ShapeType.rect, { x: cx, y: y + r * rowH, w: colWs[c], h: rowH, fill: { color }, line: { color: C.line, width: 0.6 } });
      s.addText(String(cell), { x: cx + 0.06, y: y + r * rowH + 0.06, w: colWs[c] - 0.12, h: rowH - 0.12, fontSize: opts.fontSize || 7.1, bold: r === 0, color, margin: 0, fit: 'shrink', valign: 'mid' });
      cx += colWs[c];
    });
  });
}

// 1
{
  const s = pptx.addSlide();
  s.background = { color: C.navy };
  s.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 13.333, h: 7.5, fill: { color: C.navy }, line: { color: C.navy } });
  s.addShape(pptx.ShapeType.arc, { x: 8.2, y: -0.8, w: 5.8, h: 5.8, adjustPoint: 0.22, line: { color: C.teal, transparency: 20, width: 2.5 }, rotate: 22 });
  s.addShape(pptx.ShapeType.arc, { x: 8.85, y: 2.2, w: 4.8, h: 4.8, adjustPoint: 0.25, line: { color: C.coral, transparency: 10, width: 2.0 }, rotate: 205 });
  s.addText('MediCloud EMR System', { x: 0.72, y: 1.3, w: 7.4, h: 0.65, fontSize: 35, bold: true, color: C.white, margin: 0, fit: 'shrink' });
  s.addText('Electronic Medical Record Solution for VitaCare Family Medical Centre', { x: 0.75, y: 2.1, w: 6.6, h: 0.52, fontSize: 15, color: 'CFE8F3', margin: 0, fit: 'shrink' });
  pill(s, 'Task 3 Project Presentation', 0.78, 3.05, 2.35, C.teal);
  pill(s, 'Built in VS Code', 3.32, 3.05, 1.45, C.coral);
  s.addText('Tech Hustlers', { x: 0.78, y: 4.42, w: 3.5, h: 0.32, fontSize: 15, bold: true, color: C.white, margin: 0 });
  s.addText('Acazia Ammon | Liyema Masala | Junior Rasenyalo\nXISD5319 - Work Integrated Learning', { x: 0.78, y: 4.88, w: 5.8, h: 0.55, fontSize: 9.4, color: 'CFE8F3', margin: 0, fit: 'shrink' });
  s.addShape(pptx.ShapeType.roundRect, { x: 8.55, y: 4.72, w: 3.68, h: 1.08, rectRadius: 0.08, fill: { color: '173B5A', transparency: 4 }, line: { color: '2E536F' } });
  s.addText('From paper chaos\nto digital clarity', { x: 8.88, y: 4.94, w: 3, h: 0.6, fontSize: 18, bold: true, color: C.white, align: 'center', margin: 0, fit: 'shrink' });
}

// 2
{
  const s = slide('Presentation Roadmap', 'Outline');
  const steps = ['Problem + POPIA pressure', 'MediCloud solution value', 'Architecture and security', 'Task 3 project plan', 'Feasibility and risk', 'Task 2 improvements', 'Database + prototype', 'Team and Q&A'];
  steps.forEach((t, i) => {
    const x = 0.8 + (i % 4) * 3.05, y = 1.55 + Math.floor(i / 4) * 2.05;
    s.addShape(pptx.ShapeType.roundRect, { x, y, w: 2.55, h: 1.25, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line } });
    s.addText(String(i + 1).padStart(2, '0'), { x: x + 0.18, y: y + 0.16, w: 0.55, h: 0.34, fontSize: 15, bold: true, color: i % 2 ? C.coral : C.teal, margin: 0 });
    s.addText(t, { x: x + 0.22, y: y + 0.62, w: 2.1, h: 0.34, fontSize: 9.2, bold: true, color: C.navy, margin: 0, fit: 'shrink' });
  });
}

// 3
{
  const s = slide('VitaCare’s Paper System Creates Four Business Risks', 'Problem');
  const risks = [
    ['Lost files', 'Paper records slow down consultations and can disappear at the worst moment.', C.coral],
    ['Appointment clashes', 'Manual booking makes double-booking and missed follow-ups more likely.', C.amber],
    ['Billing errors', 'Disconnected records create inaccurate invoices and slower payment collection.', C.blue],
    ['POPIA exposure', 'Weak access control and audit trails raise compliance and trust risks.', C.red]
  ];
  risks.forEach((r, i) => card(s, 0.82 + (i % 2) * 5.9, 1.45 + Math.floor(i / 2) * 1.68, 5.35, 1.18, r[0], r[1], r[2]));
  s.addShape(pptx.ShapeType.chevron, { x: 5.55, y: 4.96, w: 2.25, h: 0.55, fill: { color: C.teal }, line: { color: C.teal } });
  s.addText('Digital transformation is now operational risk management.', { x: 3.0, y: 5.72, w: 7.6, h: 0.35, fontSize: 15, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
}

// 4
{
  const s = slide('POPIA Sets the Standard: Confidentiality, Integrity, Availability', 'Compliance');
  [['Confidentiality', 'Role-based access keeps patient data limited to approved users.', C.teal], ['Integrity', 'Validated digital records reduce accidental changes and missing details.', C.blue], ['Availability', 'Cloud access keeps patient history available when care decisions happen.', C.coral]].forEach((r, i) => {
    s.addShape(pptx.ShapeType.roundRect, { x: 0.82 + i * 4.05, y: 1.55, w: 3.45, h: 2.7, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.ellipse, { x: 2.12 + i * 4.05, y: 1.95, w: 0.82, h: 0.82, fill: { color: r[2] }, line: { color: r[2] } });
    s.addText(['C','I','A'][i], { x: 2.12 + i * 4.05, y: 2.12, w: 0.82, h: 0.2, fontSize: 15, bold: true, color: C.white, align: 'center', margin: 0 });
    s.addText(r[0], { x: 1.08 + i * 4.05, y: 3.02, w: 2.9, h: 0.28, fontSize: 13.2, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
    s.addText(r[1], { x: 1.08 + i * 4.05, y: 3.47, w: 2.9, h: 0.48, fontSize: 8.3, color: C.ink, align: 'center', margin: 0.02, fit: 'shrink' });
  });
  s.addText('MediCloud addresses POPIA through secure cloud storage, authentication, RBAC and audit logging.', { x: 1.12, y: 5.2, w: 11.0, h: 0.38, fontSize: 13.2, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
}

// 5
{
  const s = slide('MediCloud EMR Converts Admin Friction Into Clinical Flow', 'Solution');
  const funcs = ['Patient records', 'Appointments', 'Prescriptions', 'Billing', 'Cloud storage', 'RBAC security'];
  funcs.forEach((f, i) => {
    const angle = (Math.PI * 2 * i) / funcs.length;
    const x = 6.1 + Math.cos(angle) * 3.2, y = 3.45 + Math.sin(angle) * 1.75;
    s.addShape(pptx.ShapeType.roundRect, { x: x - 0.86, y: y - 0.32, w: 1.72, h: 0.64, rectRadius: 0.06, fill: { color: i % 2 ? 'E8F1FF' : C.mint }, line: { color: i % 2 ? 'B9D0FF' : 'A9E8DD' } });
    s.addText(f, { x: x - 0.74, y: y - 0.12, w: 1.48, h: 0.14, fontSize: 7.2, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
  });
  s.addShape(pptx.ShapeType.ellipse, { x: 4.8, y: 2.53, w: 2.6, h: 1.65, fill: { color: C.navy }, line: { color: C.navy } });
  s.addText('MediCloud\nEMR', { x: 5.18, y: 2.98, w: 1.85, h: 0.48, fontSize: 19, bold: true, color: C.white, align: 'center', margin: 0, fit: 'shrink' });
  card(s, 0.78, 1.55, 3.18, 1.02, 'Operational', 'Faster access, fewer admin errors and no more lost files.', C.teal);
  card(s, 0.78, 2.9, 3.18, 1.02, 'Clinical', 'Complete histories and safer electronic prescriptions.', C.blue);
  card(s, 0.78, 4.25, 3.18, 1.02, 'Financial', 'Cleaner billing, faster payments and better revenue collection.', C.coral);
}

// 6
{
  const s = slide('Three-Tier Architecture Separates Experience, Logic and Data', 'Architecture');
  const tiers = [
    ['Presentation tier', 'Browser UI for doctors, nurses, patients and receptionists', C.teal],
    ['Application tier', 'Cloud server: business logic, authentication, validation and RBAC', C.blue],
    ['Data tier', 'Cloud database: patients, appointments, prescriptions and billing', C.coral]
  ];
  tiers.forEach((t, i) => {
    const y = 1.42 + i * 1.45;
    s.addShape(pptx.ShapeType.roundRect, { x: 1.12, y, w: 10.9, h: 0.94, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.rect, { x: 1.12, y, w: 0.18, h: 0.94, fill: { color: t[2] }, line: { color: t[2] } });
    s.addText(t[0], { x: 1.55, y: y + 0.2, w: 2.2, h: 0.2, fontSize: 12, bold: true, color: C.navy, margin: 0 });
    s.addText(t[1], { x: 4.05, y: y + 0.22, w: 7.35, h: 0.18, fontSize: 9, color: C.ink, margin: 0, fit: 'shrink' });
    if (i < 2) s.addText('↓', { x: 6.32, y: y + 0.95, w: 0.28, h: 0.25, fontSize: 17, bold: true, color: C.muted, align: 'center', margin: 0 });
  });
  pill(s, 'Security rule: client never accesses the database directly', 3.32, 6.0, 6.6, C.navy);
}

// 7
{
  const s = slide('Task 3 Locks the Final Plan, Report and Presentation', 'Project plan');
  const tracks = [ ['Task 1', 'Project Plan', '24 Apr', 0.95, 2.4, C.teal], ['Task 2', 'Requirements + Design', '13 May', 3.12, 3.05, C.blue], ['Task 3', 'Final Report + Presentation', 'Mid-June', 6.48, 3.0, C.coral] ];
  s.addShape(pptx.ShapeType.line, { x: 1.0, y: 3.55, w: 10.9, h: 0, line: { color: C.line, width: 2 } });
  tracks.forEach(t => {
    s.addShape(pptx.ShapeType.roundRect, { x: t[3], y: 2.18, w: t[4], h: 0.58, rectRadius: 0.06, fill: { color: t[5] }, line: { color: t[5] } });
    s.addText(t[1], { x: t[3] + 0.12, y: 2.37, w: t[4] - 0.24, h: 0.1, fontSize: 7.4, bold: true, color: C.white, align: 'center', margin: 0, fit: 'shrink' });
    s.addShape(pptx.ShapeType.ellipse, { x: t[3] + t[4] - 0.18, y: 3.36, w: 0.38, h: 0.38, fill: { color: t[5] }, line: { color: C.white, width: 1 } });
    s.addText(t[0], { x: t[3], y: 4.02, w: t[4], h: 0.22, fontSize: 10, bold: true, color: C.navy, align: 'center', margin: 0 });
    s.addText(t[2], { x: t[3], y: 4.34, w: t[4], h: 0.18, fontSize: 8, color: C.muted, align: 'center', margin: 0 });
  });
  s.addText('Final milestone: merged PDF report, PowerPoint, evaluations and presentation delivery.', { x: 1.1, y: 5.48, w: 10.8, h: 0.34, fontSize: 13, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
}

// 8
{
  const s = slide('Work Breakdown: 10.5 Weeks, 440 Hours, R66,000 Effort Budget', 'Execution');
  table(s, [
    ['Task', 'Owner', 'Duration', 'Cost'], ['Planning', 'Acazia', '1 wk', 'R6,000'], ['Requirements', 'Junior', '1.5 wks', 'R9,000'], ['System design', 'Liyema', '1.5 wks', 'R9,000'], ['Frontend + Backend', 'Liyema + Junior', '4 wks', 'R24,000'], ['Testing + Deploy', 'All / Acazia', '1.5 wks', 'R12,000'], ['Total', 'Team', '10.5 wks', 'R66,000']
  ], 0.78, 1.35, [3.15, 2.65, 2.0, 2.0], 0.55, { fontSize: 7.6, header: C.navy });
  miniMetric(s, '440h', 'total effort', 10.28, 1.6, 1.65, C.teal);
  miniMetric(s, 'R66k', 'budget', 10.28, 2.83, 1.65, C.coral);
  miniMetric(s, '10.5', 'weeks', 10.28, 4.06, 1.65, C.blue);
}

// 9
{
  const s = slide('Risks Are Managed Through Controls, Buffers and Ownership', 'Risk');
  table(s, [
    ['Risk', 'Probability', 'Impact', 'Mitigation'], ['Team member leaves', 'Low', 'High', 'Cross-train and document work'], ['Behind schedule', 'Medium', 'High', 'Buffer time + weekly checks'], ['Security breach', 'Low', 'Very high', 'RBAC, encryption, audit logs'], ['Skill gaps', 'Medium', 'Medium', 'Online learning + mentor support'], ['Scope creep', 'Medium', 'Medium', 'Change control and sign-off']
  ], 0.72, 1.28, [3.0, 1.55, 1.55, 4.55], 0.62, { fontSize: 7.4, header: C.navy });
  card(s, 1.1, 5.35, 10.8, 0.64, 'Feasibility verdict', 'Technically feasible using VS Code, Node.js, MySQL/PostgreSQL, GitHub and free-tier cloud/staging options. Economically feasible based on effort-only costing.', C.teal);
}

// 10
{
  const s = slide('Task 2 Feedback Was Converted Into Concrete Fixes', 'Improvement');
  table(s, [
    ['Feedback issue', 'Fix made'], ['Use case diagram missing', 'Added link and embedded visual artifact'], ['Input/output/process/entity tables unclear', 'Created detailed mapping tables'], ['Classes, attributes and relationships unconfirmed', 'Added complete class diagrams'], ['Entity tables missing', 'Added 9-entity domain summary'], ['Insufficient UML proof', 'Added logical model + domain class diagrams']
  ], 0.85, 1.35, [4.8, 6.4], 0.68, { fontSize: 7.8, header: C.navy });
  s.addText('Original issue: 53/100 impact. Final response: every flagged artifact now has a visible proof object.', { x: 1.1, y: 5.82, w: 10.6, h: 0.3, fontSize: 12.2, bold: true, color: C.coral, align: 'center', margin: 0, fit: 'shrink' });
}

// 11
{
  const s = slide('Normalized Data Model Supports Clinical and Admin Workflows', 'Database design');
  const entities = ['Patient', 'Doctor', 'Appointment', 'Prescription', 'Billing', 'UserAccount'];
  entities.forEach((e, i) => {
    const x = 0.92 + (i % 3) * 3.75, y = 1.45 + Math.floor(i / 3) * 1.6;
    s.addShape(pptx.ShapeType.roundRect, { x, y, w: 2.88, h: 0.82, rectRadius: 0.07, fill: { color: i % 2 ? 'E8F1FF' : C.mint }, line: { color: C.line } });
    s.addText(e, { x: x + 0.14, y: y + 0.3, w: 2.6, h: 0.12, fontSize: 10.2, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
  });
  s.addText('Core relationships: Patient 1:M Appointment, Prescription and Billing | Doctor 1:M Appointment and Prescription | UserAccount links authentication through personID.', { x: 1.05, y: 4.98, w: 10.9, h: 0.45, fontSize: 11.2, color: C.ink, align: 'center', margin: 0, fit: 'shrink' });
  pill(s, '3NF: no partial dependencies, no transitive dependencies', 3.7, 5.83, 5.95, C.teal);
}

// 12
{
  const s = slide('Prototype Preview: The VS Code Build Centers the Daily Clinic Workflow', 'Prototype');
  const screens = [['Login', 'Authenticate user by role'], ['Dashboard', 'See today’s clinic activity'], ['Register Patient', 'Capture validated patient details'], ['Book Appointment', 'Schedule, update or cancel visits']];
  screens.forEach((sc, i) => {
    const x = 0.82 + (i % 2) * 5.95, y = 1.32 + Math.floor(i / 2) * 2.05;
    s.addShape(pptx.ShapeType.roundRect, { x, y, w: 5.28, h: 1.52, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.rect, { x: x + 0.18, y: y + 0.25, w: 1.18, h: 0.9, fill: { color: i % 2 ? 'E8F1FF' : C.mint }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.line, { x: x + 0.32, y: y + 0.47, w: 0.86, h: 0, line: { color: C.muted, width: 1 } });
    s.addShape(pptx.ShapeType.line, { x: x + 0.32, y: y + 0.67, w: 0.7, h: 0, line: { color: C.muted, width: 1 } });
    s.addShape(pptx.ShapeType.roundRect, { x: x + 0.34, y: y + 0.86, w: 0.58, h: 0.16, rectRadius: 0.03, fill: { color: i % 2 ? C.blue : C.teal }, line: { color: i % 2 ? C.blue : C.teal } });
    s.addText(sc[0], { x: x + 1.62, y: y + 0.36, w: 3.25, h: 0.22, fontSize: 12, bold: true, color: C.navy, margin: 0, fit: 'shrink' });
    s.addText(sc[1], { x: x + 1.62, y: y + 0.76, w: 3.15, h: 0.2, fontSize: 8.4, color: C.ink, margin: 0, fit: 'shrink' });
  });
  s.addText('Placeholder links can be replaced with the final GitHub repository and live prototype URL before submission.', { x: 1.2, y: 5.88, w: 10.7, h: 0.24, fontSize: 9.5, italic: true, color: C.muted, align: 'center', margin: 0, fit: 'shrink' });
}

// 13
{
  const s = slide('Tech Hustlers: Clear Ownership Across Plan, Design and Build', 'Team');
  const members = [ ['Acazia Ammon', 'ST10451774', 'Project Manager', 'Planning, timeline, risk, budget and client communication', C.teal], ['Liyema Masala', 'ST10452479', 'Software Designer', 'System architecture, UI/UX, class diagrams, ERD and prototype', C.blue], ['Junior Rasenyalo', 'ST10452404', 'Software Developer', 'Database design, backend/API implementation, testing and Git', C.coral] ];
  members.forEach((m, i) => {
    const x = 0.88 + i * 4.12;
    s.addShape(pptx.ShapeType.roundRect, { x, y: 1.52, w: 3.48, h: 3.65, rectRadius: 0.08, fill: { color: C.white }, line: { color: C.line } });
    s.addShape(pptx.ShapeType.ellipse, { x: x + 1.25, y: 1.92, w: 0.98, h: 0.98, fill: { color: m[4] }, line: { color: m[4] } });
    s.addText(m[0].split(' ').map(p => p[0]).join(''), { x: x + 1.25, y: 2.24, w: 0.98, h: 0.14, fontSize: 13, bold: true, color: C.white, align: 'center', margin: 0 });
    s.addText(m[0], { x: x + 0.32, y: 3.14, w: 2.85, h: 0.22, fontSize: 12, bold: true, color: C.navy, align: 'center', margin: 0, fit: 'shrink' });
    s.addText(`${m[2]}\n${m[1]}`, { x: x + 0.38, y: 3.55, w: 2.72, h: 0.38, fontSize: 8.2, color: C.muted, align: 'center', margin: 0, fit: 'shrink' });
    s.addText(m[3], { x: x + 0.36, y: 4.25, w: 2.78, h: 0.45, fontSize: 7.8, color: C.ink, align: 'center', margin: 0.02, fit: 'shrink' });
  });
}

// 14
{
  const s = pptx.addSlide();
  s.background = { color: C.navy };
  s.addShape(pptx.ShapeType.rect, { x: 0, y: 0, w: 13.333, h: 7.5, fill: { color: C.navy }, line: { color: C.navy } });
  s.addText('The Outcome', { x: 0.82, y: 0.72, w: 3.1, h: 0.36, fontSize: 18, bold: true, color: C.teal, margin: 0 });
  s.addText('MediCloud transforms VitaCare from paper-based chaos to secure digital efficiency.', { x: 0.82, y: 1.28, w: 8.25, h: 0.95, fontSize: 29, bold: true, color: C.white, margin: 0, fit: 'shrink' });
  const wins = ['Project plan complete', 'Requirements and UML corrected', '3-tier architecture defined', '6-table 3NF database model', 'Prototype workflow ready'];
  bulletList(s, wins, 1.05, 3.0, 5.5, 1.82, { fontSize: 12.4, color: 'DDEBF5', symbol: '✓' });
  s.addText('Questions & Answers', { x: 7.32, y: 4.54, w: 4.5, h: 0.5, fontSize: 25, bold: true, color: C.white, align: 'center', margin: 0 });
  s.addText('Acazia Ammon | Liyema Masala | Junior Rasenyalo', { x: 7.42, y: 5.16, w: 4.28, h: 0.24, fontSize: 9, color: 'CFE8F3', align: 'center', margin: 0, fit: 'shrink' });
  s.addShape(pptx.ShapeType.arc, { x: 8.55, y: 0.55, w: 4.8, h: 4.8, adjustPoint: 0.22, line: { color: C.coral, width: 2, transparency: 12 }, rotate: 30 });
}

async function main() {
  await pptx.writeFile({ fileName: outPath });
  console.log(outPath);
}
main().catch(err => { console.error(err); process.exit(1); });




