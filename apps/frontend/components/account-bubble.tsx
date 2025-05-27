"use client"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import type React from "react"
import Cookies from "js-cookie"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { LogOut, UserPlus, LogIn } from "lucide-react"
import { login, logout, getUserInfo, register } from "@/lib/login"
import { useState, useEffect } from "react"

interface UserInfo {
  username: string
  email: string
  avatar?: string
}

interface LoginData {
  username: string
  password: string
}

interface RegisterData {
  username: string
  email: string
  password: string
  retypePassword: string
}

type ViewMode = "menu" | "login" | "register"

export function AccountBubble() {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false)
  const [userInfo, setUserInfo] = useState<UserInfo | null>(null)
  const [isLoading, setIsLoading] = useState<boolean>(true)
  const [viewMode, setViewMode] = useState<ViewMode>("menu")
  const [isOpen, setIsOpen] = useState<boolean>(false)
  const [loginData, setLoginData] = useState<LoginData>({ username: "", password: "" })
  const [loginError, setLoginError] = useState<string>("")
  const [registerData, setRegisterData] = useState<RegisterData>({
    username: "",
    email: "",
    password: "",
    retypePassword: "",
  })
  const [registerError, setRegisterError] = useState<string>("")

  useEffect(() => {
    checkLoginStatus()
  }, [])

  useEffect(() => {
  }, [isLoggedIn, userInfo, viewMode])

  const checkLoginStatus = async (): Promise<void> => {
    try {
      setIsLoading(true)
      const authToken =
        Cookies.get("authToken") ||
        Cookies.get("token") ||
        Cookies.get("access_token") ||
        Cookies.get("sessionid") ||
        Cookies.get("jwt")
      if (authToken) {
        try {
          const userInfoResponse = await getUserInfo()
          if (userInfoResponse) {
            setIsLoggedIn(true)
            setUserInfo(userInfoResponse)
          } else {
            clearAuthCookies()
            setIsLoggedIn(false)
            setUserInfo(null)
          }
        } catch (error) {
          console.error("getUserInfo error:", error)
          setIsLoggedIn(false)
          setUserInfo(null)
        }
      } else {
        setIsLoggedIn(false)
        setUserInfo(null)
      }
    } catch (error) {
      console.error("checkLoginStatus error:", error)
      setIsLoggedIn(false)
      setUserInfo(null)
    } finally {
      setIsLoading(false)
    }
  }
  const clearAuthCookies = () => {
    const keys = [
      "authToken",
    ]
    keys.forEach((key) => {
      Cookies.remove(key)
      Cookies.remove(key, { path: '/' })
    })
  }
  const handleLogout = async (): Promise<void> => {
    try {
      await logout()
    } catch (error) {
      console.error("Logout error:", error)
    } finally {
      clearAuthCookies()
      setIsLoggedIn(false)
      setUserInfo(null)
      setViewMode("menu")
      resetForms()
      setIsOpen(false) 
    }
  }
  const resetForms = (): void => {
    setLoginData({ username: "", password: "" })
    setRegisterData({ username: "", email: "", password: "", retypePassword: "" })
    setLoginError("")
    setRegisterError("")
  }
  const handleLoginInputChange = (field: keyof LoginData, value: string): void => {
    setLoginData((prev) => ({ ...prev, [field]: value }))
    if (loginError) setLoginError("")
  }
  const validateLoginForm = (): string | null => {
    const { username, password } = loginData
    if (!username.trim()) return "Username is required"
    if (!password) return "Password is required"
    return null
  }
  const handleLogin = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault()
    
    const validationError = validateLoginForm()
    if (validationError) {
      setLoginError(validationError)
      return
    } 
    try {
      const { username, password } = loginData 
      const response = await login(username, password)
      const { access_token, expIn } = response
      if (access_token) {
        const cookieString = `authToken=${access_token}; path=/; max-age=${expIn}`
        document.cookie = cookieString
        const verifyToken = Cookies.get("authToken")
        setIsLoggedIn(true)
        resetForms()
        setViewMode("menu")
        setIsOpen(false)
        setTimeout(async () => {
          await checkLoginStatus()
          setIsOpen(true) 
        }, 100)
      } else {
        console.error("No access token in response")
        setLoginError("Login failed - no access token received")
      }
    } catch (error) {
      console.error("Login error:", error)
      setLoginError(error instanceof Error ? error.message : "Login failed. Please try again.")
    }
  }

  const handleRegisterInputChange = (field: keyof RegisterData, value: string): void => {
    setRegisterData((prev) => ({ ...prev, [field]: value }))
    if (registerError) setRegisterError("")
  }

  const validateRegisterForm = (): string | null => {
    const { username, email, password, retypePassword } = registerData
    if (!username.trim()) return "Username is required"
    if (!email.trim()) return "Email is required"
    if (!email.includes("@")) return "Please enter a valid email"
    if (!password) return "Password is required"
    if (password.length < 6) return "Password must be at least 6 characters"
    if (password !== retypePassword) return "Passwords do not match"
    return null
  }

  const handleRegister = async (e: React.FormEvent<HTMLFormElement>): Promise<void> => {
    e.preventDefault()
    
    const validationError = validateRegisterForm()
    if (validationError) {
      setRegisterError(validationError)
      return
    }
    
    try {
      const { username, email, password } = registerData
      await register(username, email, password, password)
      resetForms()
      setViewMode("menu")
      setIsOpen(false)
      setTimeout(async () => {
        await checkLoginStatus()
        setIsOpen(true)
      }, 100)
    } catch (error) {
      console.error("Registration error:", error)
      setRegisterError(error instanceof Error ? error.message : "Registration failed. Please try again.")
    }
  }

  const getUserInitials = (): string => {
    return userInfo?.username?.slice(0, 2).toUpperCase() || "U"
  }

  const handleBackToMenu = (): void => {
    setViewMode("menu")
    resetForms()
  }

  const handleViewModeChange = (mode: ViewMode) => (e: React.MouseEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setViewMode(mode)
  }

  if (isLoading) {
    return (
      <Button variant="outline" size="sm" disabled>
        Loading...
      </Button>
    )
  }

  if (!isLoggedIn) {
    return (
      <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
        <DropdownMenuTrigger asChild>
          <Button variant="outline" size="sm" className="font-medium">
            <LogIn className="mr-2 h-4 w-4" />
            Giriş Yap
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent className="w-80 shadow-lg border bg-white" align="end" style={{backgroundColor: 'white'}} onInteractOutside={(e) => e.preventDefault()}>
          {viewMode === "menu" && (
            <>
              <DropdownMenuLabel className="py-3 px-4 bg-gray-50 border-b" style={{backgroundColor: '#f9fafb'}}>
                <span className="text-gray-900 font-semibold">Hesap</span>
              </DropdownMenuLabel>
              <div className="py-2">
                <DropdownMenuItem 
                  onClick={handleViewModeChange("login")}
                  onSelect={(e) => e.preventDefault()}
                  className="mx-2 rounded-md hover:bg-blue-50 focus:bg-blue-50 transition-colors cursor-pointer"
                >
                  <LogIn className="mr-3 h-4 w-4 text-blue-600" />
                  <span className="text-gray-700">Giriş Yap</span>
                </DropdownMenuItem>
                <DropdownMenuItem 
                  onClick={handleViewModeChange("register")}
                  onSelect={(e) => e.preventDefault()}
                  className="mx-2 rounded-md hover:bg-green-50 focus:bg-green-50 transition-colors cursor-pointer"
                >
                  <UserPlus className="mr-3 h-4 w-4 text-green-600" />
                  <span className="text-gray-700">Hesap Oluştur</span>
                </DropdownMenuItem>
              </div>
            </>
          )}

          {viewMode === "login" && (
            <div className="p-6" style={{backgroundColor: 'white'}}>
              <div className="text-center mb-6">
                <div className="inline-flex items-center justify-center w-12 h-12 bg-blue-100 rounded-full mb-3">
                  <LogIn className="h-6 w-6 text-blue-600" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900">Hoşgeldiniz</h3>
                <p className="text-sm text-gray-500 mt-1">Hesabınıza Giriş Yapın</p>
              </div>
              
              <form onSubmit={handleLogin} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="loginUsername" className="text-sm font-medium text-gray-700">
                    Kullanıcı Adı
                  </Label>
                  <Input
                    id="loginUsername"
                    type="text"
                    placeholder="Kullanıcı adınızı girin"
                    value={loginData.username}
                    onChange={(e) => handleLoginInputChange("username", e.target.value)}
                    className="h-11 text-sm border-2 border-gray-200 focus:border-blue-500 focus:ring-0 rounded-lg transition-colors"
                    required
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="loginPassword" className="text-sm font-medium text-gray-700">
                    Şifre
                  </Label>
                  <Input
                    id="loginPassword"
                    type="password"
                    placeholder="Şifrenizi girin"
                    value={loginData.password}
                    onChange={(e) => handleLoginInputChange("password", e.target.value)}
                    className="h-11 text-sm border-2 border-gray-200 focus:border-blue-500 focus:ring-0 rounded-lg transition-colors"
                    required
                  />
                </div>

                {loginError && (
                  <div className="p-3 bg-red-50 border-l-4 border-red-400 rounded-md">
                    <div className="flex">
                      <div className="flex-shrink-0">
                        <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                        </svg>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm text-red-700">{loginError}</p>
                      </div>
                    </div>
                  </div>
                )}

                <div className="space-y-3 pt-4">
                  <Button 
                    type="submit" 
                    className="w-full h-11 text-sm bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium shadow-sm transition-colors"
                  >
                    Giriş Yap
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full h-11 text-sm border-2 border-gray-300 hover:bg-gray-50 rounded-lg transition-colors"
                    onClick={handleBackToMenu}
                  >
                    İptal Et
                  </Button>
                </div>
              </form>
              
              <div className="mt-6 pt-4 border-t border-gray-200 text-center">
                <p className="text-sm text-gray-600">
                  Hesabınız Yok mu?{" "}
                  <button
                    type="button"
                    className="font-medium text-blue-600 hover:text-blue-500 transition-colors"
                    onClick={handleViewModeChange("register")}
                  >
                    Buradan Oluşturun
                  </button>
                </p>
              </div>
            </div>
          )}

          {viewMode === "register" && (
            <div className="p-6" style={{backgroundColor: 'white'}}>
              <div className="text-center mb-6">
                <div className="inline-flex items-center justify-center w-12 h-12 bg-green-100 rounded-full mb-3">
                  <UserPlus className="h-6 w-6 text-green-600" />
                </div>
                <h3 className="text-lg font-semibold text-gray-900">Hesap Oluşturun</h3>
                <p className="text-sm text-gray-500 mt-1">Bize Katılın</p>
              </div>
              
              <form onSubmit={handleRegister} className="space-y-4">
                <div className="grid grid-cols-1 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="username" className="text-sm font-medium text-gray-700">
                      Kullanıcı Adı
                    </Label>
                    <Input
                      id="username"
                      type="text"
                      placeholder="Kullanıcı adınızı girin"
                      value={registerData.username}
                      onChange={(e) => handleRegisterInputChange("username", e.target.value)}
                      className="h-10 text-sm border-2 border-gray-200 focus:border-green-500 focus:ring-0 rounded-lg transition-colors"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="registerEmail" className="text-sm font-medium text-gray-700">
                      Email Adresi
                    </Label>
                    <Input
                      id="registerEmail"
                      type="email"
                      placeholder="mailiniz@email.com"
                      value={registerData.email}
                      onChange={(e) => handleRegisterInputChange("email", e.target.value)}
                      className="h-10 text-sm border-2 border-gray-200 focus:border-green-500 focus:ring-0 rounded-lg transition-colors"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="registerPassword" className="text-sm font-medium text-gray-700">
                      Şifre
                    </Label>
                    <Input
                      id="registerPassword"
                      type="password"
                      placeholder="Min. 6 karakter"
                      value={registerData.password}
                      onChange={(e) => handleRegisterInputChange("password", e.target.value)}
                      className="h-10 text-sm border-2 border-gray-200 focus:border-green-500 focus:ring-0 rounded-lg transition-colors"
                      required
                    />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="retypePassword" className="text-sm font-medium text-gray-700">
                      Şifreyi Tekrar Girin
                    </Label>
                    <Input
                      id="retypePassword"
                      type="password"
                      placeholder="Şifrenizi tekrar girin"
                      value={registerData.retypePassword}
                      onChange={(e) => handleRegisterInputChange("retypePassword", e.target.value)}
                      className="h-10 text-sm border-2 border-gray-200 focus:border-green-500 focus:ring-0 rounded-lg transition-colors"
                      required
                    />
                  </div>
                </div>

                {registerError && (
                  <div className="p-3 bg-red-50 border-l-4 border-red-400 rounded-md">
                    <div className="flex">
                      <div className="flex-shrink-0">
                        <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                        </svg>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm text-red-700">{registerError}</p>
                      </div>
                    </div>
                  </div>
                )}

                <div className="space-y-3 pt-4">
                  <Button 
                    type="submit" 
                    className="w-full h-11 text-sm bg-green-600 hover:bg-green-700 text-white rounded-lg font-medium shadow-sm transition-colors"
                  >
                    Hesap Oluştur
                  </Button>
                  <Button
                    type="button"
                    variant="outline"
                    className="w-full h-11 text-sm border-2 border-gray-300 hover:bg-gray-50 rounded-lg transition-colors"
                    onClick={handleBackToMenu}
                  >
                    İptal
                  </Button>
                </div>
              </form>
              
              <div className="mt-6 pt-4 border-t border-gray-200 text-center">
                <p className="text-sm text-gray-600">
                  Zaten Hesabınız Var mı?{" "}
                  <button
                    type="button"
                    className="font-medium text-green-600 hover:text-green-500 transition-colors"
                    onClick={handleViewModeChange("login")}
                  >
                    Buradan Giriş Yapın
                  </button>
                </p>
              </div>
            </div>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
    )
  }
  return (
    <DropdownMenu open={isOpen} onOpenChange={setIsOpen}>
      <DropdownMenuTrigger asChild>
        <Avatar className="h-8 w-8 cursor-pointer">
          <AvatarImage
            src={userInfo?.avatar || "logo.png"}
            alt={userInfo?.username || "User"}
          />
          <AvatarFallback>{getUserInitials()}</AvatarFallback>
        </Avatar>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-80 shadow-lg border bg-white" align="end" style={{backgroundColor: 'white'}}>
        <DropdownMenuLabel className="py-4 px-4 bg-gray-50 border-b" style={{backgroundColor: '#f9fafb'}}>
          <div className="flex items-center space-x-3">
            <Avatar className="h-10 w-10">
              <AvatarImage src={userInfo?.avatar || "logo.png"} alt={userInfo?.username || "User"} />
              <AvatarFallback className="bg-blue-500 text-white">{getUserInitials()}</AvatarFallback>
            </Avatar>
            <div className="flex flex-col">
              <p className="text-sm font-semibold text-gray-900">{userInfo?.username || "User"}</p>
              <p className="text-xs text-gray-600">{userInfo?.email || ""}</p>
            </div>
          </div>
        </DropdownMenuLabel>
        <div className="py-2">
          <DropdownMenuItem 
            onClick={handleLogout}
            className="mx-2 rounded-md hover:bg-red-50 focus:bg-red-50 transition-colors text-red-600"
          >
            <LogOut className="mr-3 h-4 w-4" />
            <span>Çıkış Yap</span>
          </DropdownMenuItem>
        </div>
      </DropdownMenuContent>
    </DropdownMenu>
  )
}