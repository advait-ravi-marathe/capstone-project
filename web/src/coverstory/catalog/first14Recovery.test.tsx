import { mountFirst14Comparison } from './first14UiTestAdapter'

// Instructor browser entry point for CAP-631; the production decoder remains a learner TODO.
const root = document.getElementById('root')
if (!root) throw new Error('CS90-30 browser harness root is missing')
mountFirst14Comparison(root)
