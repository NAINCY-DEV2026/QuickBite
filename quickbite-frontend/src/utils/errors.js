// Prefers the first field-level validation message, then the server message, then a fallback
export function getErrorMessage(err, fallback = 'Something went wrong') {
  const data = err?.response?.data;
  if (data?.fieldErrors) {
    const first = Object.values(data.fieldErrors)[0];
    if (first) return first;
  }
  return data?.message || fallback;
}