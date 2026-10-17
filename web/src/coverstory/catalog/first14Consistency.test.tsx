import { mountFirst14Comparison } from './first14UiTestAdapter'

// Instructor browser entry point for CAP-630; imports the real learner component through its decoder adapter.
const root = document.getElementById('root')
if (!root) throw new Error('CS90-29 browser harness root is missing')
mountFirst14Comparison(root)
