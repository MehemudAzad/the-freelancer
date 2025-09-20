'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useForm, Controller } from 'react-hook-form';
import { motion, AnimatePresence } from 'framer-motion';
import { getProfile, updateProfile } from '@/services/profile';
import { useAppSelector } from '@/store/hooks';
import { 
  ArrowLeft, 
  Save, 
  Loader, 
  AlertCircle, 
  X, 
  User, 
  MapPin, 
  DollarSign, 
  Clock, 
  Code, 
  Languages, 
  Github, 
  Gitlab, 
  Linkedin, 
  Globe, 
  CheckCircle,
  Sparkles
} from 'lucide-react';

type ProfileFormInputs = {
  headline: string;
  bio: string;
  hourlyRateCents: number;
  currency: string;
  availability: string;
  languages: string;
  skills: string;
  locationText: string;
  githubUsername: string;
  gitlabUsername: string;
  websiteUrl: string;
  linkedinUrl: string;
};

const EditProfilePage = () => {
  const router = useRouter();
  const { user } = useAppSelector((state) => state.auth);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [success, setSuccess] = useState(false);
  const [activeSection, setActiveSection] = useState(0);

  const { control, handleSubmit, reset, watch, formState: { errors } } = useForm<ProfileFormInputs>({
    defaultValues: {
      headline: '',
      bio: '',
      hourlyRateCents: 0,
      currency: 'USD',
      availability: 'FULL_TIME',
      languages: '',
      skills: '',
      locationText: '',
      githubUsername: '',
      gitlabUsername: '',
      websiteUrl: '',
      linkedinUrl: '',
    },
  });

  useEffect(() => {
    const fetchProfile = async () => {
      if (!user?.id) {
        setError('User not authenticated');
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);
        const response = await getProfile(String(user.id));
        console.log('Profile API Response:', response);
        
        // Handle both possible response structures
        const profile = response?.profile || response;
        console.log('Profile data:', profile);
        
        if (profile && typeof profile === 'object') {
          const formData = {
            headline: profile.headline || '',
            bio: profile.bio || '',
            hourlyRateCents: profile.hourlyRateCents || 0,
            currency: profile.currency || 'USD',
            availability: profile.availability || 'FULL_TIME',
            languages: Array.isArray(profile.languages) ? profile.languages.join(', ') : '',
            skills: Array.isArray(profile.skills) ? profile.skills.join(', ') : '',
            locationText: profile.locationText || '',
            githubUsername: profile.githubUsername || '',
            gitlabUsername: profile.gitlabUsername || '',
            websiteUrl: profile.websiteUrl || '',
            linkedinUrl: profile.linkedinUrl || '',
          };
          
          console.log('Form data to be set:', formData);
          reset(formData);
        } else {
          console.warn('No valid profile data found, using defaults');
          // Reset with default values if no profile data
          reset({
            headline: '',
            bio: '',
            hourlyRateCents: 0,
            currency: 'USD',
            availability: 'FULL_TIME',
            languages: '',
            skills: '',
            locationText: '',
            githubUsername: '',
            gitlabUsername: '',
            websiteUrl: '',
            linkedinUrl: '',
          });
        }
      } catch (err: any) {
        console.error('Profile fetch error:', err);
        setError(err.message || 'Failed to load profile data.');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [user?.id, reset]);

  const onSubmit = async (data: ProfileFormInputs) => {
    setIsSubmitting(true);
    setError(null);
    try {
      const profileUpdateData = {
        ...data,
        languages: data.languages.split(',').map(s => s.trim()).filter(Boolean),
        skills: data.skills.split(',').map(s => s.trim()).filter(Boolean),
        hourlyRateCents: Number(data.hourlyRateCents),
      };
      await updateProfile(profileUpdateData);
      setSuccess(true);
      setTimeout(() => {
        router.push(`/profile/${user?.id}`);
      }, 2000);
    } catch (err: any) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const SkillPill = ({ skill, index }: { skill: string; index: number }) => (
    <motion.div
      initial={{ opacity: 0, scale: 0.8, y: 20 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      transition={{ delay: index * 0.1 }}
      className="flex items-center bg-gradient-to-r from-blue-500 to-purple-600 text-white text-sm font-medium px-4 py-2 rounded-full shadow-lg"
    >
      <Code className="w-3 h-3 mr-2" />
      {skill}
    </motion.div>
  );

  const LanguagePill = ({ language, index }: { language: string; index: number }) => (
    <motion.div
      initial={{ opacity: 0, scale: 0.8, y: 20 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      transition={{ delay: index * 0.1 }}
      className="flex items-center bg-gradient-to-r from-green-500 to-teal-600 text-white text-sm font-medium px-4 py-2 rounded-full shadow-lg"
    >
      <Languages className="w-3 h-3 mr-2" />
      {language}
    </motion.div>
  );

  const skills = watch('skills').split(',').map(s => s.trim()).filter(Boolean);
  const languages = watch('languages').split(',').map(s => s.trim()).filter(Boolean);

  const sections = [
    { id: 0, title: 'Personal Info', icon: User },
    { id: 1, title: 'Skills & Languages', icon: Code },
    { id: 2, title: 'Social Links', icon: Globe },
  ];

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-900 via-purple-900 to-pink-900">
        <motion.div
          initial={{ opacity: 0, scale: 0.8 }}
          animate={{ opacity: 1, scale: 1 }}
          className="text-center"
        >
          <motion.div
            animate={{ rotate: 360 }}
            transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
            className="w-16 h-16 border-4 border-white border-t-transparent rounded-full mx-auto mb-4"
          />
          <p className="text-white text-xl font-medium">Loading your profile...</p>
        </motion.div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-900 via-purple-900 to-pink-900 relative overflow-hidden">
      {/* Animated background elements */}
      <div className="absolute inset-0 overflow-hidden">
        <motion.div
          animate={{ 
            x: [0, 100, 0],
            y: [0, -100, 0],
            rotate: [0, 180, 360]
          }}
          transition={{ duration: 20, repeat: Infinity, ease: "linear" }}
          className="absolute top-10 left-10 w-32 h-32 bg-white/5 rounded-full"
        />
        <motion.div
          animate={{ 
            x: [0, -50, 0],
            y: [0, 100, 0],
            rotate: [0, -180, -360]
          }}
          transition={{ duration: 25, repeat: Infinity, ease: "linear" }}
          className="absolute top-1/2 right-10 w-24 h-24 bg-white/3 rounded-full"
        />
        <motion.div
          animate={{ 
            x: [0, 75, 0],
            y: [0, -75, 0],
            rotate: [0, 90, 180]
          }}
          transition={{ duration: 15, repeat: Infinity, ease: "linear" }}
          className="absolute bottom-20 left-1/3 w-20 h-20 bg-white/4 rounded-full"
        />
      </div>

      <div className="relative z-10 py-12 px-4 sm:px-6 lg:px-8">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          className="max-w-6xl mx-auto"
        >
          {/* Header */}
          <div className="flex justify-between items-center mb-12">
            <motion.button
              onClick={() => router.back()}
              whileHover={{ scale: 1.05, x: -5 }}
              whileTap={{ scale: 0.95 }}
              className="flex items-center gap-3 text-white/80 hover:text-white transition-all duration-300 bg-white/10 backdrop-blur-sm px-6 py-3 rounded-full border border-white/20"
            >
              <ArrowLeft className="h-5 w-5" />
              Back to Profile
            </motion.button>
            <motion.div
              initial={{ opacity: 0, scale: 0.8 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 0.2 }}
              className="text-center"
            >
              <h1 className="text-4xl md:text-5xl font-bold mb-2 bg-gradient-to-r from-white via-purple-200 to-pink-200 bg-clip-text text-transparent">
                Edit Your Profile
              </h1>
              <p className="text-white/70 text-lg">Make your profile shine ✨</p>
            </motion.div>
            <div className="w-32"></div> {/* Spacer for center alignment */}
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
            {/* Navigation Sidebar */}
            <motion.div
              initial={{ opacity: 0, x: -50 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.3 }}
              className="lg:col-span-1"
            >
              <div className="bg-white/10 backdrop-blur-md rounded-3xl p-6 border border-white/20 sticky top-8">
                <h3 className="text-white font-semibold mb-6 flex items-center gap-2">
                  <Sparkles className="w-5 h-5" />
                  Sections
                </h3>
                <div className="space-y-3">
                  {sections.map((section) => (
                    <motion.button
                      key={section.id}
                      onClick={() => setActiveSection(section.id)}
                      whileHover={{ scale: 1.02, x: 5 }}
                      whileTap={{ scale: 0.98 }}
                      className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 ${
                        activeSection === section.id
                          ? 'bg-white text-purple-600 shadow-lg'
                          : 'text-white/80 hover:bg-white/10 hover:text-white'
                      }`}
                    >
                      <section.icon className="w-5 h-5" />
                      {section.title}
                    </motion.button>
                  ))}
                </div>
              </div>
            </motion.div>

            {/* Form Content */}
            <div className="lg:col-span-3">
              <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
                <AnimatePresence mode="wait">
                  {/* Personal Information Section */}
                  {activeSection === 0 && (
                    <motion.div
                      key="personal"
                      initial={{ opacity: 0, x: 50 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -50 }}
                      transition={{ duration: 0.3 }}
                      className="bg-white/10 backdrop-blur-md border border-white/20 rounded-3xl p-8 shadow-2xl"
                    >
                      <div className="flex items-center gap-3 mb-8">
                        <div className="p-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-full">
                          <User className="w-6 h-6 text-white" />
                        </div>
                        <h2 className="text-3xl font-bold text-white">Personal Information</h2>
                      </div>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <div className="md:col-span-2">
                          <label htmlFor="headline" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Sparkles className="w-4 h-4" />
                            Professional Headline
                          </label>
                          <Controller
                            name="headline"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="headline" 
                                className="glass-input" 
                                placeholder="e.g., Senior Frontend Developer specializing in React" 
                              />
                            )}
                          />
                        </div>
                        
                        <div className="md:col-span-2">
                          <label htmlFor="bio" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <User className="w-4 h-4" />
                            About You
                          </label>
                          <Controller
                            name="bio"
                            control={control}
                            render={({ field }) => (
                              <textarea 
                                {...field} 
                                id="bio" 
                                rows={6} 
                                className="glass-input resize-none" 
                                placeholder="Tell clients about your experience, passion, and what makes you unique..."
                              />
                            )}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="hourlyRateCents" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <DollarSign className="w-4 h-4" />
                            Hourly Rate (USD)
                          </label>
                          <Controller
                            name="hourlyRateCents"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="hourlyRateCents" 
                                type="number" 
                                min="1"
                                onChange={e => field.onChange(Number(e.target.value) * 100)} 
                                value={field.value / 100} 
                                className="glass-input" 
                                placeholder="50"
                              />
                            )}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="availability" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Clock className="w-4 h-4" />
                            Availability
                          </label>
                          <Controller
                            name="availability"
                            control={control}
                            render={({ field }) => (
                              <select {...field} id="availability" className="glass-input">
                                <option value="FULL_TIME">Full-time (40+ hrs/week)</option>
                                <option value="PART_TIME">Part-time (20-39 hrs/week)</option>
                                <option value="CONTRACT">Contract basis</option>
                              </select>
                            )}
                          />
                        </div>
                        
                        <div className="md:col-span-2">
                          <label htmlFor="locationText" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <MapPin className="w-4 h-4" />
                            Location
                          </label>
                          <Controller
                            name="locationText"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="locationText" 
                                className="glass-input" 
                                placeholder="e.g., San Francisco, CA or Remote" 
                              />
                            )}
                          />
                        </div>
                      </div>
                    </motion.div>
                  )}

                  {/* Skills & Languages Section */}
                  {activeSection === 1 && (
                    <motion.div
                      key="skills"
                      initial={{ opacity: 0, x: 50 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -50 }}
                      transition={{ duration: 0.3 }}
                      className="bg-white/10 backdrop-blur-md border border-white/20 rounded-3xl p-8 shadow-2xl"
                    >
                      <div className="flex items-center gap-3 mb-8">
                        <div className="p-3 bg-gradient-to-r from-green-500 to-teal-600 rounded-full">
                          <Code className="w-6 h-6 text-white" />
                        </div>
                        <h2 className="text-3xl font-bold text-white">Skills & Languages</h2>
                      </div>
                      
                      <div className="space-y-8">
                        <div>
                          <label htmlFor="skills" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Code className="w-4 h-4" />
                            Technical Skills (comma-separated)
                          </label>
                          <Controller
                            name="skills"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="skills" 
                                className="glass-input" 
                                placeholder="e.g., React, Node.js, TypeScript, Python, AWS" 
                              />
                            )}
                          />
                          <div className="mt-4 flex flex-wrap gap-3">
                            {skills.map((skill, i) => <SkillPill key={i} skill={skill} index={i} />)}
                          </div>
                        </div>
                        
                        <div>
                          <label htmlFor="languages" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Languages className="w-4 h-4" />
                            Languages (comma-separated)
                          </label>
                          <Controller
                            name="languages"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="languages" 
                                className="glass-input" 
                                placeholder="e.g., English, Spanish, French" 
                              />
                            )}
                          />
                          <div className="mt-4 flex flex-wrap gap-3">
                            {languages.map((language, i) => <LanguagePill key={i} language={language} index={i} />)}
                          </div>
                        </div>
                      </div>
                    </motion.div>
                  )}

                  {/* Social Links Section */}
                  {activeSection === 2 && (
                    <motion.div
                      key="social"
                      initial={{ opacity: 0, x: 50 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -50 }}
                      transition={{ duration: 0.3 }}
                      className="bg-white/10 backdrop-blur-md border border-white/20 rounded-3xl p-8 shadow-2xl"
                    >
                      <div className="flex items-center gap-3 mb-8">
                        <div className="p-3 bg-gradient-to-r from-pink-500 to-orange-500 rounded-full">
                          <Globe className="w-6 h-6 text-white" />
                        </div>
                        <h2 className="text-3xl font-bold text-white">Social Links</h2>
                      </div>
                      
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                        <div>
                          <label htmlFor="githubUsername" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Github className="w-4 h-4" />
                            GitHub Username
                          </label>
                          <Controller
                            name="githubUsername"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="githubUsername" 
                                className="glass-input" 
                                placeholder="your-github-username" 
                              />
                            )}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="gitlabUsername" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Gitlab className="w-4 h-4" />
                            GitLab Username
                          </label>
                          <Controller
                            name="gitlabUsername"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="gitlabUsername" 
                                className="glass-input" 
                                placeholder="your-gitlab-username" 
                              />
                            )}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="linkedinUrl" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Linkedin className="w-4 h-4" />
                            LinkedIn Profile
                          </label>
                          <Controller
                            name="linkedinUrl"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="linkedinUrl" 
                                className="glass-input" 
                                placeholder="https://linkedin.com/in/your-profile" 
                              />
                            )}
                          />
                        </div>
                        
                        <div>
                          <label htmlFor="websiteUrl" className="flex items-center gap-2 text-sm font-medium text-white/90 mb-3">
                            <Globe className="w-4 h-4" />
                            Personal Website
                          </label>
                          <Controller
                            name="websiteUrl"
                            control={control}
                            render={({ field }) => (
                              <input 
                                {...field} 
                                id="websiteUrl" 
                                className="glass-input" 
                                placeholder="https://yourwebsite.com" 
                              />
                            )}
                          />
                        </div>
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Navigation and Submit */}
                <motion.div
                  initial={{ opacity: 0, y: 30 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.5 }}
                  className="flex justify-between items-center pt-8"
                >
                  <div className="flex gap-4">
                    {activeSection > 0 && (
                      <motion.button
                        type="button"
                        onClick={() => setActiveSection(activeSection - 1)}
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        className="flex items-center gap-2 px-6 py-3 bg-white/10 text-white border border-white/30 rounded-full hover:bg-white/20 transition-all duration-300"
                      >
                        <ArrowLeft className="w-4 h-4" />
                        Previous
                      </motion.button>
                    )}
                    
                    {activeSection < sections.length - 1 && (
                      <motion.button
                        type="button"
                        onClick={() => setActiveSection(activeSection + 1)}
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        className="flex items-center gap-2 px-6 py-3 bg-white text-purple-600 rounded-full hover:bg-white/90 transition-all duration-300 font-medium"
                      >
                        Next
                        <ArrowLeft className="w-4 h-4 rotate-180" />
                      </motion.button>
                    )}
                  </div>

                  <motion.button
                    type="submit"
                    disabled={isSubmitting}
                    whileHover={{ scale: isSubmitting ? 1 : 1.05 }}
                    whileTap={{ scale: isSubmitting ? 1 : 0.95 }}
                    className="flex items-center justify-center gap-3 px-8 py-4 bg-gradient-to-r from-green-500 to-teal-600 text-white rounded-full font-bold text-lg shadow-2xl hover:shadow-green-500/25 transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isSubmitting ? (
                      <>
                        <motion.div
                          animate={{ rotate: 360 }}
                          transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                        >
                          <Loader className="w-5 h-5" />
                        </motion.div>
                        Saving...
                      </>
                    ) : (
                      <>
                        <Save className="w-5 h-5" />
                        Save Profile
                      </>
                    )}
                  </motion.button>
                </motion.div>

                {/* Error Message */}
                <AnimatePresence>
                  {error && (
                    <motion.div
                      initial={{ opacity: 0, y: 20, scale: 0.9 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -20, scale: 0.9 }}
                      className="bg-red-500/20 border border-red-500/50 text-red-100 p-6 rounded-2xl flex items-center gap-4 backdrop-blur-sm"
                    >
                      <AlertCircle className="h-6 w-6 flex-shrink-0" />
                      <p className="font-medium">{error}</p>
                    </motion.div>
                  )}
                </AnimatePresence>

                {/* Success Message */}
                <AnimatePresence>
                  {success && (
                    <motion.div
                      initial={{ opacity: 0, y: 20, scale: 0.9 }}
                      animate={{ opacity: 1, y: 0, scale: 1 }}
                      exit={{ opacity: 0, y: -20, scale: 0.9 }}
                      className="bg-green-500/20 border border-green-500/50 text-green-100 p-6 rounded-2xl flex items-center gap-4 backdrop-blur-sm"
                    >
                      <CheckCircle className="h-6 w-6 flex-shrink-0" />
                      <p className="font-medium">Profile updated successfully! Redirecting...</p>
                    </motion.div>
                  )}
                </AnimatePresence>
              </form>
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

export default EditProfilePage;

// Add custom styles for the glass effect
const styles = `
  .glass-input {
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 1rem;
    padding: 1rem;
    color: white;
    font-size: 1rem;
    width: 100%;
    transition: all 0.3s ease;
  }
  
  .glass-input:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.15);
    border-color: rgba(255, 255, 255, 0.4);
    box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.1);
  }
  
  .glass-input::placeholder {
    color: rgba(255, 255, 255, 0.6);
  }
  
  .glass-input option {
    background: rgba(30, 30, 30, 0.9);
    color: white;
  }
`;

// Inject styles
if (typeof document !== 'undefined') {
  const styleElement = document.createElement('style');
  styleElement.textContent = styles;
  document.head.appendChild(styleElement);
}
