-- U011 supplied acceptance query: exact document count in each stored version.
SELECT version, COUNT(*) AS document_count
FROM documents
GROUP BY version
ORDER BY version;
