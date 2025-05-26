import { MakeRequest, MakeAuthenticatedRequest } from "@/lib/request";

async function login(username: string, password: string) {
    const response = await MakeRequest('/login', 'POST', {
        username: username,
        password: password,
    });
    
    if (response instanceof Error) {
        throw new Error(`Login failed: ${response.message}`);
    }
    
    return response;
    }

async function register(username: string, password: string, repassword: string, email:string) {
    const response = await MakeRequest('/register', 'POST', {
        username: username,
        password: password,
        repassword: repassword,
        email: email,
    });
    
    if (response instanceof Error) {
        throw new Error(`Registration failed: ${response.message}`);
    }
    
    return response;
}
async function getUserInfo() {
    const response = await MakeAuthenticatedRequest('/user', 'GET');
    
    if (response instanceof Error) {
        throw new Error(`Get user info failed: ${response.message}`);
    }
    
    return response;
}