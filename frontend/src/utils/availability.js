export const DAYS = [
  ['MONDAY', 'Mon'], ['TUESDAY', 'Tue'], ['WEDNESDAY', 'Wed'], ['THURSDAY', 'Thu'],
  ['FRIDAY', 'Fri'], ['SATURDAY', 'Sat'], ['SUNDAY', 'Sun']
];
const SHORT = Object.fromEntries(DAYS);
const ORDER = Object.fromEntries(DAYS.map(([key], i) => [key, i]));

const hhmm = (t) => String(t).slice(0, 5); // the API sends "09:00:00"

/** Groups the flat list from GET /doctors/availability by doctor id. */
export function groupByDoctor(rows) {
  const map = {};
  for (const r of rows) (map[r.doctorId] ||= []).push(r);
  for (const id of Object.keys(map)) {
    map[id].sort((a, b) => ORDER[a.day] - ORDER[b.day] || hhmm(a.start).localeCompare(hhmm(b.start)));
  }
  return map;
}

/** "Mon 09:00–13:00" for each block, in week order. */
export function formatSlots(slots) {
  return (slots || []).map((s) => `${SHORT[s.day]} ${hhmm(s.start)}–${hhmm(s.end)}`);
}

export const toForm = (s) => ({ day: s.day, start: hhmm(s.start), end: hhmm(s.end) });
