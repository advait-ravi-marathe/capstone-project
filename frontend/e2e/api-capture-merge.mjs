/** Matches cloned fetch bodies to Playwright response records exactly once per document. */
export function createApiCaptureMerger(apiResponses) {
  const mergedResponses = new WeakSet();
  const consumedCaptureIndicesByDocument = new Map();

  return (captures, documentId) => {
    let consumedIndices = consumedCaptureIndicesByDocument.get(documentId);
    if (!consumedIndices) {
      consumedIndices = new Set();
      consumedCaptureIndicesByDocument.set(documentId, consumedIndices);
    }

    for (const [index, capture] of captures.entries()) {
      if (consumedIndices.has(index)) continue;
      const response = apiResponses.find((candidate) => !mergedResponses.has(candidate)
        && candidate.method === capture.method && candidate.path === capture.path
        && candidate.status === capture.status);
      if (!response) continue;

      response.body = capture.body;
      mergedResponses.add(response);
      consumedIndices.add(index);
    }
  };
}
