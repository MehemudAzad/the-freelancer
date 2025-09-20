'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Eye, EyeOff, UserCircle, Mail, Lock, ArrowLeft } from 'lucide-react';
import { useAppDispatch } from '@/store/hooks';
import { loginSuccess } from '@/store/authSlice';
import { motion } from 'framer-motion';
import { getBackendUrl } from '@/config/api';

const SignupPage = () => {
  const router = require('next/navigation').useRouter();
  const dispatch = useAppDispatch();
  const [showPassword, setShowPassword] = useState(false);
  // const [form, setForm] = useState({ name: '', email: '', password: '', role: 'freelancer' });
  const [form, setForm] = useState({
  name: '',
  handle: '',  // Added handle
  email: '',
  password: '',
  role: 'freelancer',
});

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true); setError(''); setSuccess('');

    try {
      const res = await fetch(`${getBackendUrl()}/api/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      if (!res.ok) throw new Error((await res.json()).message || 'Registration failed');

      setSuccess('Registration successful! Logging you in...');

      const loginRes = await fetch(`${getBackendUrl()}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, password: form.password }),
      });
      if (!loginRes.ok) throw new Error('Auto-login failed');

      const loginData = await loginRes.json();
      localStorage.setItem('accessToken', loginData.accessToken);
      localStorage.setItem('refreshToken', loginData.refreshToken);
      dispatch(loginSuccess({
        user: loginData.user,
        accessToken: loginData.accessToken,
        refreshToken: loginData.refreshToken,
      }));

      setForm({ name: '', email: '', handle: '', password: '', role: 'freelancer' });
      router.push('/dashboard');
    } catch (err: any) {
      setError(err.message || 'Something went wrong');
      if (success) setTimeout(() => router.push('/auth/login'), 2000);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-tr from-purple-200 via-pink-100 to-yellow-100 px-4">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
        className="relative bg-white rounded-3xl shadow-2xl w-full max-w-md p-8 overflow-hidden border border-white/20"
        style={{ backdropFilter: 'blur(15px)' }}
      >
        {/* Floating gradient circles */}
        <div className="absolute -top-16 -left-16 w-40 h-40 bg-gradient-to-r from-purple-400 to-pink-400 rounded-full opacity-30 animate-pulse-slow"></div>
        <div className="absolute -bottom-16 -right-16 w-40 h-40 bg-gradient-to-r from-yellow-400 to-orange-400 rounded-full opacity-30 animate-pulse-slow"></div>

        {/* Back Link */}
        <Link href="/" className="flex items-center mb-6 text-blue-600 hover:underline relative z-10">
          <ArrowLeft className="h-4 w-4 mr-2" /> Back to Home
        </Link>

        {/* Heading */}
        <h2 className="text-3xl font-extrabold mb-2 text-center text-transparent bg-clip-text bg-gradient-to-r from-purple-600 to-pink-600 relative z-10">
          Create an Account
        </h2>
        <p className="text-gray-500 mb-6 text-center relative z-10">
          Sign up to start your freelance journey!
        </p>

        <form onSubmit={handleSubmit} className="space-y-5 relative z-10">
          {error && <div className="text-red-500 text-center mb-2 font-medium">{error}</div>}
          {success && <div className="text-green-600 text-center mb-2 font-medium">{success}</div>}

          {/* Name */}
          <div>
            <label className="block mb-1 font-medium text-gray-700">Name</label>
            <div className="relative">
              <Input name="name" type="text" placeholder="Your Name" value={form.name} onChange={handleChange} className="pl-10 shadow-inner border-gray-300 focus:ring-2 focus:ring-purple-300 rounded-xl" required />
              <UserCircle className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
            </div>
          </div>

          {/* Handle / Username */}
          <div>
            <label className="block mb-1 font-medium text-gray-700">Handle*</label>
            <div className="relative">
              <Input
                name="handle"
                type="text"
                placeholder="Your unique handle (e.g., khalid123)"
                value={form.handle || ''}
                onChange={handleChange}
                className="pl-10 shadow-inner border-gray-300 focus:ring-2 focus:ring-purple-300 rounded-xl"
                required
              />
              <UserCircle className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
            </div>
            <p className="text-xs text-gray-400 mt-1">
              Must be unique and contain no spaces. Example: khalid123
            </p>
          </div>


          {/* Email */}
          <div>
            <label className="block mb-1 font-medium text-gray-700">Email</label>
            <div className="relative">
              <Input name="email" type="email" placeholder="you@email.com" value={form.email} onChange={handleChange} className="pl-10 shadow-inner border-gray-300 focus:ring-2 focus:ring-purple-300 rounded-xl" required />
              <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
            </div>
          </div>

          {/* Password */}
          <div>
            <label className="block mb-1 font-medium text-gray-700">Password</label>
            <div className="relative">
              <Input
                name="password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Password"
                value={form.password}
                onChange={handleChange}
                className="pl-10 pr-10 shadow-inner border-gray-300 focus:ring-2 focus:ring-purple-300 rounded-xl"
                required
              />
              <Lock className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-5 w-5" />
              <button type="button" onClick={() => setShowPassword((prev) => !prev)} className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 focus:outline-none" tabIndex={-1}>
                {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
              </button>
            </div>
          </div>

          {/* Role */}
          <div>
            <label className="block mb-1 font-medium text-gray-700">Role</label>
            <div className="flex space-x-4">
              <label className="flex items-center gap-2">
                <input type="radio" name="role" value="freelancer" checked={form.role === 'freelancer'} onChange={handleChange} className="accent-purple-600" />
                Freelancer
              </label>
              <label className="flex items-center gap-2">
                <input type="radio" name="role" value="client" checked={form.role === 'client'} onChange={handleChange} className="accent-pink-600" />
                Client
              </label>
            </div>
          </div>

          {/* Submit */}
          <Button
            type="submit"
            disabled={loading}
            className="w-full bg-gradient-to-r from-purple-600 to-pink-500 text-white font-bold py-3 rounded-2xl shadow-lg hover:scale-105 transform transition duration-300"
          >
            {loading ? 'Signing Up...' : 'Sign Up'}
          </Button>
        </form>

        {/* Login link */}
        <p className="mt-6 text-center text-gray-600 relative z-10">
          Already have an account?{' '}
          <Link href="/auth/login" className="text-purple-600 font-semibold hover:underline">
            Login
          </Link>
        </p>
      </motion.div>
    </div>
  );
};

export default SignupPage;
