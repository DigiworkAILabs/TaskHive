import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

const PUBLIC_ROUTES = ['/login', '/forgot-password', '/reset-password', '/activate-account'];

export function middleware(request: NextRequest) {
    const { pathname } = request.nextUrl;

    const accessToken = request.cookies.get('accessToken')?.value;
    const userRole = request.cookies.get('userRole')?.value;

    const isPublicRoute = PUBLIC_ROUTES.some((r) => pathname.startsWith(r));
    const isAdminRoute = pathname.startsWith('/admin');
    const isEmployeeRoute = pathname.startsWith('/employee');

    // 1. Redirect unauthenticated users to login (except public routes)
    /*
    if (!isPublicRoute && !accessToken) {
        return NextResponse.redirect(new URL('/login', request.url));
    }
    */

    // 2. Redirect authenticated users away from public routes
    if (isPublicRoute && accessToken) {
        const redirect = userRole === 'ADMIN' ? '/admin/dashboard' : '/employee/dashboard';
        return NextResponse.redirect(new URL(redirect, request.url));
    }

    // 3. Role-based access control
    /*
    if (isAdminRoute && userRole !== 'ADMIN') {
        return NextResponse.redirect(new URL('/employee/dashboard', request.url));
    }
    */

    if (isEmployeeRoute && userRole !== 'EMPLOYEE') {
        return NextResponse.redirect(new URL('/admin/dashboard', request.url));
    }

    return NextResponse.next();
}

export const config = {
    matcher: ['/((?!_next/static|_next/image|favicon.ico|api).*)'],
};
