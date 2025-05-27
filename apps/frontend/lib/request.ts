const login_base_url = 'http://localhost:8080'
const path_base_url ='http://localhost:8081'

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
    console.log(url)
    const baseUrl = url.startsWith('/login') || url.startsWith('/register') || url.startsWith('/user') ? login_base_url : path_base_url;
    url = `${baseUrl}${url}`

    const response = await fetch(url, {
      method,
      headers,
      body: body ? JSON.stringify(body) : null,
    })

    if (!response.ok) {
      const errorData = await response.text()
      try {
        const jsonError = JSON.parse(errorData)
        return new Error(jsonError?.message || `API request failed: ${response.statusText}`)
      } catch (e) {
        return new Error(`API request failed: ${response.statusText}. Response: ${errorData.substring(0, 100)}`)
      }
    }
    
    const contentType = response.headers.get('content-type')
    if (contentType && contentType.includes('application/json')) {
      return response.json()
    } else {
      return response.text()
    }
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
    const accessToken = document.cookie
      .split('; ')
      .find(row => row.startsWith('authToken='))
      ?.split('=')[1]

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${accessToken}`,
      ...extraHeaders,
    }
    const baseUrl = url.startsWith('/login') || url.startsWith('/register') || url.startsWith('/user') ? login_base_url : path_base_url;
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
