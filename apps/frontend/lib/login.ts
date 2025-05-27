import { MakeRequest, MakeAuthenticatedRequest } from "@/lib/request";

export async function login(username: string, password: string) {
    const response = await MakeRequest('/login', 'POST', {
        username: username,
        password: password,
    });
    
    if (response instanceof Error) {
        throw new Error(`Login failed: ${response.message}`);
    }
    
    return response;
    }

export async function register(username: string, email:string, password: string, repassword: string) {
    const response = await MakeRequest('/register', 'POST', {
        username: username,
        email: email,
        password: password,
        repassword: repassword,
    });
    
    if (response instanceof Error) {
        throw new Error(`Registration failed: ${response.message}`);
    }
    
    return response;
}
export async function getUserInfo() {
    const response = await MakeAuthenticatedRequest('/user', 'GET', null);
    
    if (response instanceof Error) {
        throw new Error(`Get user info failed: ${response.message}`);
    }
    
    return response;
}
export async function logout(){
    document.cookie.split(";").forEach((cookie) => {
        const eqPos = cookie.indexOf("=");
        const name = eqPos > -1 ? cookie.substr(0, eqPos) : cookie;
        document.cookie = `${name}=;expires=Thu, 01 Jan 1970 00:00:00 GMT;path=/`;
    });
}