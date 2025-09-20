'use client';

import React, { useState, useRef, useEffect } from 'react';
import { useAppSelector, useAppDispatch } from '@/store/hooks';
import { motion } from 'framer-motion';
import { getBackendUrl } from '@/config/api';
import { 
  User, 
  Mail, 
  Briefcase, 
  Star, 
  Settings, 
  Camera, 
  CheckCircle, 
  Upload,
  MapPin,
  DollarSign,
  Clock,
  Award,
  Code,
  Calendar,
  Globe,
  Github,
  Linkedin,
  ArrowLeft,
  Edit3
} from 'lucide-react';
import { updateProfilePicture, getProfile } from '@/services/profile';
import { updateUser } from '@/store/authSlice';
import { useRouter } from 'next/navigation';

interface ProfileData {
  headline: string;
  bio: string;
  hourlyRateCents: number;
  currency: string;
  availability: string;
  languages: string[] | null;
  skills: string[];
  locationText: string;
  githubUsername: string | null;
  gitlabUsername: string | null;
  websiteUrl: string | null;
  linkedinUrl: string | null;
  deliveryScore: number;
  reviewAvg: number;
  reviewsCount: number;
  profilePictureUrl: string | null;
}

const ProfilePage = () => {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const dispatch = useAppDispatch();
  const [previewImage, setPreviewImage] = useState<string | null>(null);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);
  const [profileData, setProfileData] = useState<ProfileData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      if (user?.id) {
        setLoading(true);
        setError('');
        try {
          const accessToken = typeof window !== 'undefined' ? localStorage.getItem('accessToken') : '';
          const axios = (await import('axios')).default;
          const res = await axios.get(`${getBackendUrl()}/api/profiles/${user.id}`, {
            headers: { Authorization: `Bearer ${accessToken}` },
          });
          setProfileData(res.data);
        } catch (err: any) {
          setError(err.response?.data?.message || 'Failed to fetch profile.');
          console.error('Failed to fetch profile data:', err);
        } finally {
          setLoading(false);
        }
      }
    };

    fetchProfile();
  }, [user?.id]);

  const handleFileChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (file) {
      setSelectedFile(file);
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreviewImage(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleImageClick = () => {
    fileInputRef.current?.click();
  };

  const handleUpload = async () => {
    if (!selectedFile) return;

    setIsUploading(true);
    try {
      const updatedUser = await updateProfilePicture(selectedFile);
      dispatch(updateUser(updatedUser));
      // Refetch profile data to get the new image URL
      const data = await getProfile(String(user!.id));
      setProfileData(data);
      setPreviewImage(null);
      setSelectedFile(null);
    } catch (error) {
      console.error('Failed to upload profile picture:', error);
      // You might want to show an error message to the user
    } finally {
      setIsUploading(false);
    }
  };

  if (!isAuthenticated || !user) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gray-100">
        <p className="text-xl text-gray-600">Please log in to view your profile.</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-3 border-blue-500 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 flex items-center justify-center">
        <div className="text-center">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 max-w-md">
            <p className="text-red-600">{error}</p>
            <button
              onClick={() => router.push('/dashboard')}
              className="mt-4 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              Back to Dashboard
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-cyan-50 px-4 py-8">
      <div className="max-w-6xl mx-auto">
        
        {/* Header */}
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="mb-8"
        >
          <div className="flex items-center justify-between mb-6">
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => router.push('/dashboard')}
              className="flex items-center gap-2 text-gray-600 hover:text-gray-800 transition-colors"
            >
              <ArrowLeft className="h-5 w-5" />
              Back to Dashboard
            </motion.button>
            
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => router.push('/profile/edit')}
              className="flex items-center gap-2 bg-blue-500 text-white px-4 py-2 rounded-lg hover:bg-blue-600 transition-colors"
            >
              <Edit3 className="h-4 w-4" />
              Edit Profile
            </motion.button>
          </div>
        </motion.div>

        {/* Profile Header Card */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, delay: 0.1 }}
          className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-8 mb-8"
        >
          <div className="flex flex-col md:flex-row items-center md:items-start gap-8">
            {/* Profile Picture */}
            <motion.div
              whileHover={{ scale: 1.05 }}
              className="relative group"
              onClick={handleImageClick}
            >
              <img
                src={previewImage || profileData?.profilePictureUrl || `https://ui-avatars.com/api/?name=${user.name}&background=random`}
                alt={user?.name || 'Profile'}
                className="w-32 h-32 rounded-full object-cover border-4 border-white shadow-lg cursor-pointer"
              />
              <div className="absolute inset-0 bg-black/50 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer">
                <Camera className="w-8 h-8 text-white" />
              </div>
              <div className="absolute -bottom-2 -right-2 bg-green-500 w-8 h-8 rounded-full border-4 border-white flex items-center justify-center">
                <div className="w-3 h-3 bg-white rounded-full"></div>
              </div>
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                className="hidden"
                accept="image/*"
              />
            </motion.div>

            {/* Basic Info */}
            <div className="flex-1 text-center md:text-left">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{user?.name}</h1>
              <p className="text-xl text-blue-600 font-medium mb-3">{profileData?.headline || 'Professional Freelancer'}</p>
              
              <div className="flex flex-wrap items-center justify-center md:justify-start gap-4 text-gray-600 mb-4">
                <div className="flex items-center gap-1">
                  <MapPin className="h-4 w-4" />
                  <span>{profileData?.locationText || 'Location not set'}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Mail className="h-4 w-4" />
                  <span>{user?.email}</span>
                </div>
                <div className="flex items-center gap-1">
                  <Calendar className="h-4 w-4" />
                  <span>Joined {profileData?.createdAt ? new Date(profileData.createdAt).toLocaleDateString() : 'Recently'}</span>
                </div>
              </div>

              {/* Stats */}
              <div className="flex flex-wrap justify-center md:justify-start gap-6">
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-yellow-500 mb-1">
                    <Star className="h-4 w-4 fill-current" />
                    <span className="font-bold">{profileData?.reviewAvg || '0.0'}</span>
                  </div>
                  <p className="text-sm text-gray-600">{profileData?.reviewsCount || 0} reviews</p>
                </div>
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-green-600 mb-1">
                    <DollarSign className="h-4 w-4" />
                    <span className="font-bold">${profileData?.hourlyRateCents ? (profileData.hourlyRateCents / 100) : '0'}/hr</span>
                  </div>
                  <p className="text-sm text-gray-600">Hourly Rate</p>
                </div>
                <div className="text-center">
                  <div className="flex items-center justify-center gap-1 text-blue-600 mb-1">
                    <Award className="h-4 w-4" />
                    <span className="font-bold">{profileData?.deliveryScore || '0.0'}</span>
                  </div>
                  <p className="text-sm text-gray-600">Delivery Score</p>
                </div>
              </div>

              {/* Upload button */}
              {previewImage && (
                <motion.button
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  onClick={handleUpload}
                  disabled={isUploading}
                  className="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:bg-indigo-400"
                >
                  {isUploading ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Uploading...
                    </>
                  ) : (
                    <>
                      <Upload className="w-5 h-5 mr-2" />
                      Save Picture
                    </>
                  )}
                </motion.button>
              )}
            </div>
          </div>

          {/* Bio */}
          {profileData?.bio && (
            <div className="mt-6 pt-6 border-t border-gray-200">
              <p className="text-gray-700 leading-relaxed">{profileData.bio}</p>
            </div>
          )}
        </motion.div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column */}
          <div className="lg:col-span-2 space-y-8">
            
            {/* Skills */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.2 }}
              className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-6"
            >
              <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center gap-3">
                <Code className="h-6 w-6 text-blue-500" />
                Skills & Expertise
              </h2>
              {profileData?.skills && profileData.skills.length > 0 ? (
                <div className="flex flex-wrap gap-3">
                  {profileData.skills.map((skill: string, index: number) => (
                    <motion.span
                      key={index}
                      initial={{ opacity: 0, scale: 0.8 }}
                      animate={{ opacity: 1, scale: 1 }}
                      transition={{ duration: 0.3, delay: index * 0.1 }}
                      className="bg-gradient-to-r from-blue-500 to-purple-600 text-white px-4 py-2 rounded-full text-sm font-medium shadow-md hover:shadow-lg transition-all duration-300"
                    >
                      {skill}
                    </motion.span>
                  ))}
                </div>
              ) : (
                <p className="text-gray-500 italic">No skills listed yet.</p>
              )}
            </motion.div>

            {/* Languages */}
            {profileData?.languages && profileData.languages.length > 0 && (
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.6, delay: 0.3 }}
                className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-6"
              >
                <h2 className="text-2xl font-bold text-gray-900 mb-4 flex items-center gap-3">
                  <Globe className="h-6 w-6 text-green-500" />
                  Languages
                </h2>
                <div className="flex flex-wrap gap-3">
                  {profileData.languages.map((language: string, index: number) => (
                    <span
                      key={index}
                      className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-sm font-medium"
                    >
                      {language}
                    </span>
                  ))}
                </div>
              </motion.div>
            )}

          </div>

          {/* Right Column */}
          <div className="space-y-8">
            
            {/* Availability */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.4 }}
              className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-6"
            >
              <h2 className="text-xl font-bold text-gray-900 mb-4 flex items-center gap-3">
                <Clock className="h-5 w-5 text-orange-500" />
                Availability
              </h2>
              <div className="space-y-3">
                <div className={`px-3 py-2 rounded-lg text-center font-medium ${
                  profileData?.availability === 'PART_TIME' 
                    ? 'bg-orange-100 text-orange-700' 
                    : 'bg-green-100 text-green-700'
                }`}>
                  {profileData?.availability?.replace('_', ' ') || 'Not Set'}
                </div>
                <p className="text-sm text-gray-600 text-center">
                  Currently accepting new projects
                </p>
              </div>
            </motion.div>

            {/* Social Links */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.5 }}
              className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-6"
            >
              <h2 className="text-xl font-bold text-gray-900 mb-4">Connect</h2>
              <div className="space-y-3">
                {profileData?.githubUsername && (
                  <a
                    href={`https://github.com/${profileData.githubUsername}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-lg bg-gray-50 hover:bg-gray-100 transition-colors"
                  >
                    <Github className="h-5 w-5 text-gray-700" />
                    <span className="text-gray-700">GitHub</span>
                  </a>
                )}
                {profileData?.gitlabUsername && (
                  <a
                    href={`https://gitlab.com/${profileData.gitlabUsername}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-lg bg-orange-50 hover:bg-orange-100 transition-colors"
                  >
                    <Code className="h-5 w-5 text-orange-600" />
                    <span className="text-orange-600">GitLab</span>
                  </a>
                )}
                {profileData?.linkedinUrl && (
                  <a
                    href={profileData.linkedinUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-lg bg-blue-50 hover:bg-blue-100 transition-colors"
                  >
                    <Linkedin className="h-5 w-5 text-blue-600" />
                    <span className="text-blue-600">LinkedIn</span>
                  </a>
                )}
                {profileData?.websiteUrl && (
                  <a
                    href={profileData.websiteUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex items-center gap-3 p-3 rounded-lg bg-purple-50 hover:bg-purple-100 transition-colors"
                  >
                    <Globe className="h-5 w-5 text-purple-600" />
                    <span className="text-purple-600">Website</span>
                  </a>
                )}
                {!profileData?.githubUsername && !profileData?.gitlabUsername && 
                 !profileData?.linkedinUrl && !profileData?.websiteUrl && (
                  <p className="text-gray-500 italic text-center py-4">No social links added yet</p>
                )}
              </div>
            </motion.div>

            {/* Quick Stats */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.6, delay: 0.6 }}
              className="bg-white/90 backdrop-blur-md border border-gray-100 rounded-2xl shadow-xl p-6"
            >
              <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Stats</h2>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">Currency</span>
                  <span className="font-bold text-blue-600">{profileData?.currency || 'USD'}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">Response Time</span>
                  <span className="font-bold text-green-600">Within 1 hour</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">Last Updated</span>
                  <span className="font-bold text-gray-700">
                    {profileData?.updatedAt ? new Date(profileData.updatedAt).toLocaleDateString() : 'Recently'}
                  </span>
                </div>
              </div>
            </motion.div>

          </div>
        </div>

      </div>
    </div>
  );
};

export default ProfilePage;
