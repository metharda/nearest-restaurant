const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8081';

export async function MakeRequest(
  url: string,
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET',
  body: unknown = null,
  extraHeaders: Record<string, string> = {}
) {
  try {
    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...extraHeaders,
    }

    url = `${baseUrl}${url}`

    const response = await fetch(url, {
      method,
      headers,
      body: body ? JSON.stringify(body) : null,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => null)
      return new Error(errorData?.message || `API request failed: ${response.statusText}`)
    }

    return response.json()
  } catch (error) {
    console.error('API Request Error:', error)
    return error
  }
}

export async function MakeAuthenticatedRequest(
  url: string,
  method: 'GET' | 'POST' | 'PUT' | 'DELETE' = 'GET',
  body: unknown = null,
  extraHeaders: Record<string, string> = {}
) {
  try {
    const accessToken = localStorage.getItem('accessToken')

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
      ...extraHeaders,
    }

    url = `${baseUrl}${url}`

    const response = await fetch(url, {
      method,
      headers,
      body: method !== 'GET' && body ? JSON.stringify(body) : null,
    })

    if (!response.ok) {
      const errorData = await response.json().catch(() => null)
      return new Error(errorData?.message || `API request failed: ${response.statusText}`)
    }

    return response.json()
  } catch (error) {
    console.error('API Request Error:', error)
    return error
  }
}
