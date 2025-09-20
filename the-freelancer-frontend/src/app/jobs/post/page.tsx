
'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAppSelector } from '@/store/hooks';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import jobService from '@/services/job';
import { CheckCircle, AlertCircle, ChevronRight, DollarSign, Clock, Calendar, Paperclip, Eye, Plus, X, PlayCircle, PauseCircle, StopCircle, RefreshCw, Wand2, Loader2 } from 'lucide-react';
import { motion } from 'framer-motion';

export default function PostJobPage() {
  const router = useRouter();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);
  const [authChecked, setAuthChecked] = useState(false);
  const [formData, setFormData] = useState({
    projectName: '',
    description: '',
    category: '',
    skills: [] as string[],
    isUrgent: false,
    budgetType: 'FIXED' as ('FIXED' | 'HOURLY'),
    minBudgetCents: 0,
    maxBudgetCents: 0,
    ndaRequired: false,
    ipAssignment: true,
    deadline: '',
    expectedDuration: '',
    status: 'OPEN' as ('OPEN' | 'DRAFT'),
  });
  const [currentSkill, setCurrentSkill] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [pdfPreviewUrl, setPdfPreviewUrl] = useState<string | null>(null);
  const [enhancingDescription, setEnhancingDescription] = useState(false);
  const [enhancingTitle, setEnhancingTitle] = useState(false);
  const [suggestingSkills, setSuggestingSkills] = useState(false);
  const [aiSuggestedSkills, setAiSuggestedSkills] = useState<string[]>([]);

  // Suggested skills based on common categories
  const suggestedSkills = [
    'Full Stack Development', 'Mobile App Development', 'User Experience Research',
    'User Interface / IA', 'Website Development'
  ];

  // Initialize auth state from localStorage if Redux state is empty
  useEffect(() => {
    // If already authenticated in Redux, mark as checked
    if (isAuthenticated && user) {
      setAuthChecked(true);
      console.log("Auth already loaded in Redux:", { name: user.name, role: user.role });

      // Check if user is a client (case insensitive)
      const userRole = user.role?.toLowerCase();
      if (userRole !== 'client') {
        console.log("Redirecting: User is not a client, role is:", user.role);
        // Commenting out redirect to allow testing - uncomment if needed
        // router.push('/jobs');
      }
    } else {
      // Try to load from localStorage if needed
      const userString = localStorage.getItem('user');
      const accessToken = localStorage.getItem('accessToken');

      if (userString && accessToken) {
        try {
          const localUser = JSON.parse(userString);
          console.log("Loaded user from localStorage:", localUser);

          const userRole = localUser.role?.toLowerCase();
          if (userRole !== 'client') {
            console.log("Redirecting: User from localStorage is not a client, role is:", localUser.role);
            // Commenting out redirect to allow testing - uncomment if needed
            // router.push('/jobs');
          }

          setAuthChecked(true);
        } catch (e) {
          console.error("Error parsing user from localStorage:", e);
          setAuthChecked(true); // Still mark as checked to show login form
        }
      } else {
        console.log("No auth data in localStorage");
        setAuthChecked(true); // Mark as checked to show login form
      }
    }
  }, [isAuthenticated, user, router]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: checked,
    }));
  };

  const handleSelectChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleAddSkill = () => {
    if (currentSkill.trim() && !formData.skills.includes(currentSkill.trim()) && formData.skills.length < 10) {
      setFormData((prev) => ({
        ...prev,
        skills: [...prev.skills, currentSkill.trim()],
      }));
      setCurrentSkill('');
    }
  };

  // PDF file input handler
  const handlePdfChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    if (file && file.type === 'application/pdf') {
      setPdfFile(file);
      setPdfPreviewUrl(URL.createObjectURL(file));
    } else {
      setPdfFile(null);
      setPdfPreviewUrl(null);
    }
  };

  const handleAddSkillFromSuggestion = (skill: string) => {
    if (!formData.skills.includes(skill) && formData.skills.length < 10) {
      setFormData((prev) => ({
        ...prev,
        skills: [...prev.skills, skill],
      }));
    }
  };

  const handleRemoveSkill = (skill: string) => {
    setFormData((prev) => ({
      ...prev,
      skills: prev.skills.filter((s: string) => s !== skill),
    }));
  };

  const validateForm = () => {
    if (!formData.projectName.trim()) {
      setError('Project name is required');
      return false;
    }
    if (formData.projectName.length < 5) {
      setError('Project name must be at least 5 characters long');
      return false;
    }
    if (!formData.description.trim()) {
      setError('Job description is required');
      return false;
    }
    if (formData.minBudgetCents <= 0 || formData.maxBudgetCents <= 0) {
      setError('Budget amounts must be greater than 0');
      return false;
    }
    if (formData.minBudgetCents >= formData.maxBudgetCents) {
      setError('Maximum budget must be greater than minimum budget');
      return false;
    }
    return true;
  };

  const enhanceJobDescription = async () => {
    if (!formData.description.trim()) {
      setError('Please enter a basic description first to enhance it with AI');
      return;
    }

    setEnhancingDescription(true);
    setError('');

    try {
      const response = await fetch('/api/ai/enhance-description', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          description: formData.description
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to enhance description');
      }

      const data = await response.json();
      console.log('AI enhancement response:', data);
      
      if (data.enhancedDescription) {
        setFormData({ ...formData, description: data.enhancedDescription });
        setError(''); // Clear any existing errors
        // You could add a success toast here if you have a toast system
      } else {
        throw new Error('No enhanced description received from AI');
      }
    } catch (error) {
      console.error('Error enhancing description:', error);
      setError('Failed to enhance description. Please try again.');
    } finally {
      setEnhancingDescription(false);
    }
  };

  const enhanceJobTitle = async () => {
    console.log('Enhance job title clicked');
    if (!formData.description.trim()) {
      setError('Please enter a job description first to enhance the title with AI');
      return;
    }

    setEnhancingTitle(true);
    setError('');
    console.log('Starting title enhancement with description:', formData.description);

    try {
      console.log('Making request to /api/ai/enhance-title');
      const response = await fetch('/api/ai/enhance-title', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          description: formData.description
        }),
      });

      console.log('Response status:', response.status);
      if (!response.ok) {
        const errorText = await response.text();
        console.error('API Error Response:', errorText);
        throw new Error(`Failed to enhance title: ${response.status} ${errorText}`);
      }

      const data = await response.json();
      console.log('AI title enhancement response:', data);
      
      if (data.suggestedTitle) {
        setFormData(prev => ({ ...prev, projectName: data.suggestedTitle }));
        setError(''); // Clear any existing errors
        // You could add a success toast here if you have a toast system
      } else {
        throw new Error('No enhanced title received from AI');
      }
    } catch (error) {
      console.error('Error enhancing title:', error);
      setError(`Failed to enhance title: ${error instanceof Error ? error.message : 'Unknown error'}`);
    } finally {
      setEnhancingTitle(false);
    }
  };

  const suggestSkillsWithAI = async () => {
    if (!formData.description.trim()) {
      setError('Please enter a job description first to get AI skill suggestions');
      return;
    }

    setSuggestingSkills(true);
    setError('');

    try {
      const response = await fetch('/api/ai/suggest-skills', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          description: formData.description
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to suggest skills');
      }

      const data = await response.json();
      console.log('AI skills suggestion response:', data);
      
      if (data.suggestedSkills && Array.isArray(data.suggestedSkills)) {
        setAiSuggestedSkills(data.suggestedSkills);
        setError(''); // Clear any existing errors
      } else {
        throw new Error('No skills suggestions received from AI');
      }
    } catch (error) {
      console.error('Error suggesting skills:', error);
      setError('Failed to suggest skills. Please try again.');
    } finally {
      setSuggestingSkills(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate form
    if (!validateForm()) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Convert budget to cents
      const minBudget = parseFloat(formData.minBudgetCents.toString()) * 100;
      const maxBudget = parseFloat(formData.maxBudgetCents.toString()) * 100;

      const jobDataObject = {
        ...formData,
        minBudgetCents: minBudget,
        maxBudgetCents: maxBudget,
      };

      if (pdfFile) {
        const form = new FormData();
        const jobBlob = new Blob([JSON.stringify(jobDataObject)], { type: 'application/json' });
        
        form.append('job', jobBlob);
        form.append('file', pdfFile, pdfFile.name);

        await jobService.jobAPI.createJob(form);
      } else {
        // Regular job creation without attachment
        await jobService.jobAPI.createJob(jobDataObject);
      }
      setSuccess(true);

      // Redirect to dashboard after success (like gig creation)
      setTimeout(() => {
        router.push('/dashboard');
      }, 1500);
    } catch (err: any) {
      console.error('Error posting job:', err);
      setError(err.response?.data?.message || 'Failed to post job. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  // Display loading spinner while auth is being checked
  if (!authChecked) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-blue-500"></div>
      </div>
    );
  }

  // Check if user is not authenticated from Redux or localStorage
  const userString = localStorage.getItem('user');
  const isLocalStorageAuthenticated = !!userString && !!localStorage.getItem('accessToken');

  if (!isAuthenticated && !isLocalStorageAuthenticated) {
    console.log("Not authenticated in Redux or localStorage");

    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <AlertCircle className="h-12 w-12 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-center mb-4">Authentication Required</h1>
          <p className="text-gray-600 text-center mb-6">
            You need to be logged in as a client to post a job.
          </p>
          <div className="flex justify-center">
            <Button
              onClick={() => router.push('/auth/login')}
              className="bg-blue-600 hover:bg-blue-700 text-white"
            >
              Log In
            </Button>
          </div>
        </div>
      </div>
    );
  }

  // Only clients can post jobs
  if (user && user.role?.toLowerCase() !== 'client') {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full">
          <AlertCircle className="h-12 w-12 text-yellow-500 mx-auto mb-4" />
          <h1 className="text-2xl font-bold text-center mb-4">Access Denied</h1>
          <p className="text-gray-600 text-center mb-6">
            Only clients can post jobs. Your account type is {user.role}.
          </p>
          <div className="flex justify-center">
            <Button
              onClick={() => router.push('/jobs')}
              className="bg-blue-600 hover:bg-blue-700 text-white"
            >
              Browse Jobs
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-100 via-white to-green-100">
        <motion.div
          initial={{ opacity: 0, y: 40, scale: 0.95 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.5 }}
          className="bg-white/80 backdrop-blur-xl border border-gray-200 rounded-2xl shadow-2xl p-10 max-w-md w-full mx-4"
        >
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
          >
            <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4" />
          </motion.div>
          <motion.h1
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            className="text-2xl font-bold text-center mb-4"
          >
            🚀 Job Posted Successfully!
          </motion.h1>
          <motion.p
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className="text-gray-600 text-center mb-6"
          >
            Your job has been posted and is now visible to freelancers.
          </motion.p>
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.5 }}
            className="flex justify-center"
          >
            <Button
              onClick={() => router.push('/dashboard')}
              className="bg-blue-600 hover:bg-blue-700 text-white"
            >
              Go to Dashboard
            </Button>
          </motion.div>
        </motion.div>
      </div>
    );
  }

  // Preview Modal Component
  const PreviewModal = () => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
      <motion.div
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{ opacity: 1, scale: 1 }}
        className="bg-white rounded-xl shadow-2xl max-w-3xl w-full max-h-[80vh] overflow-y-auto"
      >
        <div className="p-6 border-b border-gray-200 flex justify-between items-center">
          <h2 className="text-xl font-bold text-gray-900">Job Preview</h2>
          <Button
            type="button"
            onClick={() => setShowPreview(false)}
            variant="outline"
            size="sm"
          >
            <X className="h-4 w-4" />
          </Button>
        </div>

        <div className="p-6 space-y-6">
          <div>
            <h3 className="text-2xl font-bold text-gray-900">{formData.projectName || 'Project Name'}</h3>
            {formData.isUrgent && (
              <div className="inline-block bg-orange-100 text-orange-800 px-2 py-1 rounded-full text-xs font-medium mt-2">
                🚀 URGENT
              </div>
            )}
          </div>

          <div>
            <h4 className="font-semibold text-gray-800 mb-2">Description</h4>
            <p className="text-gray-600 whitespace-pre-wrap">{formData.description || 'No description provided'}</p>
          </div>

          {formData.skills.length > 0 && (
            <div>
              <h4 className="font-semibold text-gray-800 mb-2">Required Skills</h4>
              <div className="flex flex-wrap gap-2">
                {formData.skills.map((skill: string) => (
                  <span key={skill} className="bg-blue-100 text-blue-800 text-sm px-2 py-1 rounded-full">
                    {skill}
                  </span>
                ))}
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <h4 className="font-semibold text-gray-800 mb-2">Budget</h4>
              <p className="text-gray-600">
                ${formData.minBudgetCents} - ${formData.maxBudgetCents} ({formData.budgetType})
              </p>
            </div>
            <div>
              <h4 className="font-semibold text-gray-800 mb-2">Category</h4>
              <p className="text-gray-600">{formData.category || 'Not specified'}</p>
            </div>
          </div>

          {/* Job Status */}
          <div>
            <h4 className="font-semibold text-gray-800 mb-2">Job Status</h4>
            <div className="flex items-center space-x-2">
              {(() => {
                const statusConfig = {
                  'OPEN': { label: 'Open', icon: PlayCircle, color: 'text-green-600 bg-green-100' },
                  'DRAFT': { label: 'Draft', icon: PauseCircle, color: 'text-yellow-600 bg-yellow-100' },
                }[formData.status] || { label: 'Open', icon: PlayCircle, color: 'text-green-600 bg-green-100' };
                
                const IconComponent = statusConfig.icon;
                return (
                  <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${statusConfig.color}`}>
                    <IconComponent className="w-4 h-4 mr-1" />
                    {statusConfig.label}
                  </span>
                );
              })()}
            </div>
          </div>

          {(formData.deadline || formData.expectedDuration) && (
            <div className="grid grid-cols-2 gap-4">
              {formData.deadline && (
                <div>
                  <h4 className="font-semibold text-gray-800 mb-2">Deadline</h4>
                  <p className="text-gray-600">{new Date(formData.deadline).toLocaleDateString()}</p>
                </div>
              )}
              {formData.expectedDuration && (
                <div>
                  <h4 className="font-semibold text-gray-800 mb-2">Expected Duration</h4>
                  <p className="text-gray-600">{formData.expectedDuration}</p>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="p-6 border-t border-gray-200 flex justify-end space-x-2">
          <Button type="button" onClick={() => setShowPreview(false)} variant="outline">
            Edit More
          </Button>
          <Button type="button" onClick={() => { setShowPreview(false); document.getElementById('jobForm')?.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true })); }} className="bg-blue-600 hover:bg-blue-700 text-white">
            Post Job
          </Button>
        </div>
      </motion.div>
    </div>
  );

  return (
    <>
      {showPreview && <PreviewModal />}
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-100 via-white to-green-100 px-4 py-10">
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          className="bg-white/80 backdrop-blur-xl border border-gray-200 rounded-2xl shadow-2xl p-10 w-full max-w-4xl"
        >
          <div className="text-center mb-10">
            <h2 className="text-5xl font-extrabold bg-gradient-to-r from-purple-600 via-pink-500 to-red-500 bg-clip-text text-transparent mb-3 drop-shadow-sm tracking-tight">
              Post a New Job
            </h2>
            <p className="text-gray-600 text-base max-w-md mx-auto leading-relaxed">
              Find the perfect freelancer for your project by creating a detailed job post.
            </p>
          </div>


          {error && (
            <div className="mb-6 bg-red-50 border-l-4 border-red-500 p-4 rounded-lg">
              <div className="flex items-center">
                <AlertCircle className="h-5 w-5 text-red-500 mr-2" />
                <p className="text-red-700">{error}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            {/* PDF Attachment Upload */}
            <div>
              <label htmlFor="pdfAttachment" className="block text-sm font-medium text-gray-700 mb-2">
                Attach PDF (optional)
              </label>
              <input
                id="pdfAttachment"
                type="file"
                accept="application/pdf"
                onChange={handlePdfChange}
                className="block w-full text-sm text-gray-900 border border-gray-300 rounded-lg cursor-pointer bg-gray-50 focus:outline-none"
              />
              {pdfFile && pdfPreviewUrl && (
                <div className="mt-3 flex items-center space-x-3 bg-gray-100 p-3 rounded-lg">
                  <Paperclip className="w-5 h-5 text-blue-500" />
                  <span className="text-gray-800 font-medium truncate max-w-xs">{pdfFile.name}</span>
                  <a href={pdfPreviewUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline text-sm font-semibold">
                    Preview
                  </a>
                  <a href={pdfPreviewUrl} download={pdfFile.name} className="text-green-600 hover:underline text-sm font-semibold">
                    Download
                  </a>
                  <button type="button" onClick={() => { setPdfFile(null); setPdfPreviewUrl(null); }} className="ml-2 text-red-500 hover:text-red-700">
                    <X className="w-4 h-4" />
                  </button>
                </div>
              )}
              <p className="text-xs text-gray-500 mt-1">Only PDF files are supported for now.</p>
            </div>
            <div>
              <div className="flex items-center justify-between mb-2">
                <label htmlFor="projectName" className="block text-sm font-medium text-gray-700">
                  Project Name*
                </label>
                
                <Button
                  type="button"
                  onClick={enhanceJobTitle}
                  disabled={enhancingTitle || !formData.description.trim()}
                  className="flex items-center space-x-2 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {enhancingTitle ? (
                    <>
                      <Loader2 className="h-3 w-3 animate-spin" />
                      <span>Enhancing...</span>
                    </>
                  ) : (
                    <>
                      <Wand2 className="h-3 w-3" />
                      <span>Enhance with AI</span>
                    </>
                  )}
                </Button>
              </div>
              
              <Input
                id="projectName"
                name="projectName"
                value={formData.projectName}
                onChange={handleInputChange}
                placeholder="e.g., Full-Stack E-commerce Website Development"
                required
                className="w-full rounded-xl border border-transparent bg-gradient-to-r from-indigo-50 via-purple-50 to-pink-50 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-indigo-200 hover:from-indigo-100 hover:via-purple-100 hover:to-pink-100 transition-all duration-300 ease-in-out p-3"
              />
              <p className="text-xs text-gray-500 mt-1">
                Keep it clear and specific to attract the right freelancers. Use "Enhance with AI" to improve your title based on the job description.
              </p>
            </div>

            {/* Job Status Selection */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-3">
                Job Status*
              </label>
              <div className="grid grid-cols-2 md:grid-cols-5 gap-3 justify-between">
                {[
                  { value: 'OPEN', label: 'Open', icon: PlayCircle, color: 'from-green-500 to-emerald-500', bgColor: 'from-green-50 to-emerald-50' },
                  { value: 'DRAFT', label: 'Draft', icon: PauseCircle, color: 'from-yellow-400 to-yellow-500', bgColor: 'from-yellow-50 to-yellow-100' },
                ].map((status) => {
                  const IconComponent = status.icon;
                  const isSelected = formData.status === status.value;
                  
                  return (
                    <motion.button
                      key={status.value}
                      type="button"
                      onClick={() => setFormData(prev => ({ ...prev, status: status.value as any }))}
                      className={`
                        relative p-4 rounded-2xl border-2 transition-all duration-300 group
                        ${isSelected 
                          ? `border-transparent bg-gradient-to-r ${status.color} text-white shadow-xl transform scale-105`
                          : `border-gray-200 bg-gradient-to-r ${status.bgColor} text-gray-700 hover:border-gray-300 hover:shadow-lg`
                        }
                      `}
                      whileHover={{ scale: isSelected ? 1.05 : 1.02 }}
                      whileTap={{ scale: 0.98 }}
                    >
                      <div className="flex flex-col items-center space-y-2">
                        <IconComponent 
                          className={`h-6 w-6 transition-all duration-300 ${
                            isSelected ? 'text-white' : 'text-gray-600 group-hover:text-gray-800'
                          }`} 
                        />
                        <span className={`text-sm font-medium text-center ${
                          isSelected ? 'text-white' : 'text-gray-700 group-hover:text-gray-900'
                        }`}>
                          {status.label}
                        </span>
                      </div>
                      
                      {/* Selected indicator */}
                      {isSelected && (
                        <motion.div
                          initial={{ scale: 0, rotate: 180 }}
                          animate={{ scale: 1, rotate: 0 }}
                          className="absolute top-2 right-2 w-6 h-6 bg-white/20 rounded-full flex items-center justify-center"
                        >
                          <CheckCircle className="h-4 w-4 text-white" />
                        </motion.div>
                      )}
                    </motion.button>
                  );
                })}
              </div>
              <p className="text-xs text-gray-500 mt-2">
                Choose the current status of your job posting.
              </p>
            </div>


            <div>
              <div className="flex items-center justify-between mb-2">
                <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                  Job Description*
                </label>
                
                <Button
                  type="button"
                  onClick={enhanceJobDescription}
                  disabled={enhancingDescription || !formData.description.trim()}
                  className="flex items-center space-x-2 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white px-4 py-2 rounded-lg font-medium transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {enhancingDescription ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span>Enhancing...</span>
                    </>
                  ) : (
                    <>
                      <Wand2 className="h-4 w-4" />
                      <span>Enhance with AI</span>
                    </>
                  )}
                </Button>
              </div>
              
              <Textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="Describe your project, requirements, deliverables, and any other relevant details..."
                required
                className="w-full min-h-[200px] rounded-xl border border-transparent bg-gradient-to-br from-purple-50 via-pink-50 to-red-50 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-purple-200 hover:from-purple-100 hover:via-pink-100 hover:to-red-100 transition-all duration-300 ease-in-out p-3"
              />
              <p className="text-xs text-gray-500 mt-1">
                Be detailed about project scope, timeline, and specific requirements. Use "Enhance with AI" to improve your description.
              </p>
            </div>


            <div>
              <div className="flex items-center justify-between mb-3">
                <label htmlFor="skills" className="block text-2xl font-extrabold bg-gradient-to-r from-purple-600 via-pink-500 to-red-500 bg-clip-text text-transparent">
                  What skills are required?
                </label>
                
                <Button
                  type="button"
                  onClick={suggestSkillsWithAI}
                  disabled={suggestingSkills || !formData.description.trim()}
                  className="flex items-center space-x-2 bg-gradient-to-r from-blue-600 to-purple-600 hover:from-blue-700 hover:to-purple-700 text-white px-4 py-2 rounded-lg font-medium transition-all duration-300 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {suggestingSkills ? (
                    <>
                      <Loader2 className="h-4 w-4 animate-spin" />
                      <span>Suggesting...</span>
                    </>
                  ) : (
                    <>
                      <Wand2 className="h-4 w-4" />
                      <span>AI Suggest Skills</span>
                    </>
                  )}
                </Button>
              </div>
              <p className="text-gray-600 text-base mb-5">
                We’ve detected the following skills to suit your project. Modify or add up to 10 skills to best match your needs.
              </p>

              {/* Skills Selection Area */}
              <div className="border border-gray-100 rounded-2xl bg-gradient-to-br from-purple-50 via-pink-50 to-red-50 p-6 shadow-sm space-y-4 transition-all duration-300">
                {/* Selected Skills Row */}
                <div className="flex flex-wrap gap-3">
                  {formData.skills.map((skill: string) => (
                    <span
                      key={skill}
                      className="bg-white border border-gray-200 text-gray-800 px-4 py-2 rounded-full flex items-center text-sm font-medium shadow-sm hover:shadow-md transition-all duration-200"
                    >
                      {skill}
                      <button
                        type="button"
                        className="ml-2 text-gray-400 hover:text-red-500 transition-colors"
                        onClick={() => handleRemoveSkill(skill)}
                      >
                        ×
                      </button>
                    </span>
                  ))}

                  {/* Add Custom Skill Input */}
                  {formData.skills.length < 10 && (
                    <Input
                      value={currentSkill}
                      onChange={(e) => setCurrentSkill(e.target.value)}
                      placeholder="Add a skill..."
                      className="flex-1 min-w-[200px] max-w-[300px] bg-white text-gray-900 border border-gray-200 rounded-xl shadow-sm focus:border-purple-400 focus:ring-2 focus:ring-purple-200 placeholder-gray-400 transition-all duration-300 ease-in-out"
                      onKeyPress={(e) => {
                        if (e.key === 'Enter') {
                          e.preventDefault();
                          handleAddSkill();
                        }
                      }}
                    />
                  )}
                </div>

                {/* Skills Counter */}
                <p className="text-sm text-gray-600">
                  {formData.skills.length}/10 skills selected
                </p>
              </div>

              {/* AI Suggested Skills */}
              {aiSuggestedSkills.length > 0 && (
                <div className="mt-5">
                  <p className="text-base text-gray-700 mb-2">
                    <strong className="font-semibold text-gray-800">✨ AI Suggested skills:</strong>{' '}
                    <span className="space-x-2">
                      {aiSuggestedSkills
                        .filter(skill => !formData.skills.includes(skill))
                        .map((skill, index) => (
                          <button
                            key={skill}
                            type="button"
                            onClick={() => handleAddSkillFromSuggestion(skill)}
                            className="inline-block bg-gradient-to-r from-blue-500 to-purple-500 text-white px-3 py-1 rounded-full text-xs font-medium hover:from-blue-600 hover:to-purple-600 transition-all duration-200 disabled:opacity-40"
                            disabled={formData.skills.length >= 10}
                          >
                            {skill}
                          </button>
                        ))}
                    </span>
                  </p>
                </div>
              )}

              {/* Default Suggested Skills */}
              {/* <div className="mt-5">
                <p className="text-base text-gray-700 mb-2">
                  <strong className="font-semibold text-gray-800">Suggested skills:</strong>{' '}
                  <span className="space-x-2">
                    {suggestedSkills
                      .filter(skill => !formData.skills.includes(skill))
                      .map((skill, index) => (
                        <button
                          key={skill}
                          type="button"
                          onClick={() => handleAddSkillFromSuggestion(skill)}
                          className="inline-block bg-gradient-to-r from-pink-500 to-red-500 text-white px-3 py-1 rounded-full text-xs font-medium hover:from-pink-600 hover:to-red-600 transition-all duration-200 disabled:opacity-40"
                          disabled={formData.skills.length >= 10}
                        >
                          {skill}
                        </button>
                      ))}
                  </span>
                </p>
              </div> */}
            </div>


            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
                  Category
                </label>
                <select
                  id="category"
                  name="category"
                  value={formData.category}
                  onChange={handleSelectChange}
                  className="w-full rounded-xl border border-transparent bg-gradient-to-r from-purple-100 via-pink-100 to-red-100 py-2 px-3 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-purple-200 hover:from-purple-200 hover:via-pink-200 hover:to-red-200 transition-all duration-300 ease-in-out"
                >
                  <option value="">Select a category</option>
                  <option value="Web Development">Web Development</option>
                  <option value="Mobile Development">Mobile Development</option>
                  <option value="AI & Machine Learning">AI & Machine Learning</option>
                  <option value="Graphic Design">Graphic Design</option>
                  <option value="Digital Marketing">Digital Marketing</option>
                  <option value="Writing & Translation">Writing & Translation</option>
                  <option value="Video & Animation">Video & Animation</option>
                  <option value="Music & Audio">Music & Audio</option>
                  <option value="Data Science">Data Science</option>
                  <option value="Business">Business</option>
                  <option value="Photography">Photography</option>
                </select>
              </div>


              <div>
                <label htmlFor="budgetType" className="block text-sm font-medium text-gray-700 mb-2">
                  Budget Type*
                </label>
                <select
                  id="budgetType"
                  name="budgetType"
                  value={formData.budgetType}
                  onChange={handleSelectChange}
                  required
                  className="w-full rounded-xl border border-transparent bg-gradient-to-r from-green-100 via-blue-100 to-purple-100 py-2 px-3 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-green-200 hover:from-green-200 hover:via-blue-200 hover:to-purple-200 transition-all duration-300 ease-in-out"
                >
                  <option value="FIXED">Fixed Price</option>
                  <option value="HOURLY">Hourly Rate</option>
                </select>
              </div>

            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label className="block text-base font-semibold text-gray-800 mb-1">
                  Minimum Budget ($)
                </label>
                <Input
                  id="minBudgetCents"
                  name="minBudgetCents"
                  type="number"
                  value={formData.minBudgetCents}
                  onChange={handleInputChange}
                  placeholder="e.g., 500"
                  required
                  className="w-full rounded-xl border border-transparent bg-gradient-to-r from-green-50 via-blue-50 to-purple-50 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-green-200 hover:from-green-100 hover:via-blue-100 hover:to-purple-100 transition-all duration-300 ease-in-out p-3"
                />
              </div>

              <div className="space-y-2">
                <label className="block text-base font-semibold text-gray-800 mb-1">
                  Maximum Budget ($)
                </label>
                <Input
                  id="maxBudgetCents"
                  name="maxBudgetCents"
                  type="number"
                  value={formData.maxBudgetCents}
                  onChange={handleInputChange}
                  placeholder="e.g., 1500"
                  required
                  className="w-full rounded-xl border border-transparent bg-gradient-to-r from-green-50 via-blue-50 to-purple-50 text-gray-900 shadow-sm focus:outline-none focus:ring-4 focus:ring-green-200 hover:from-green-100 hover:via-blue-100 hover:to-purple-100 transition-all duration-300 ease-in-out p-3"
                />
              </div>
            </div>


            <div className="space-y-4 pt-4">
              <div className="flex items-center justify-between bg-white border border-gray-200 p-3 rounded-lg">
                <div>
                  <label htmlFor="isUrgent" className="font-medium text-gray-700">
                    Urgent Job
                  </label>
                  <p className="text-xs text-gray-500">Mark this job as urgent to get faster responses.</p>
                </div>
                <label htmlFor="isUrgent" className="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" id="isUrgent" name="isUrgent" checked={formData.isUrgent} onChange={handleCheckboxChange} className="sr-only peer" />
                  <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-focus:ring-4 peer-focus:ring-blue-300 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="flex items-center justify-between bg-white border border-gray-200 p-3 rounded-lg">
                <div>
                  <label htmlFor="ndaRequired" className="font-medium text-gray-700">
                    NDA Required
                  </label>
                  <p className="text-xs text-gray-500">Require freelancers to sign a Non-Disclosure Agreement.</p>
                </div>
                <label htmlFor="ndaRequired" className="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" id="ndaRequired" name="ndaRequired" checked={formData.ndaRequired} onChange={handleCheckboxChange} className="sr-only peer" />
                  <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-focus:ring-4 peer-focus:ring-blue-300 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>

              <div className="flex items-center justify-between bg-white border border-gray-200 p-3 rounded-lg">
                <div>
                  <label htmlFor="ipAssignment" className="font-medium text-gray-700">
                    IP Assignment
                  </label>
                  <p className="text-xs text-gray-500">Assign intellectual property rights to you upon completion.</p>
                </div>
                <label htmlFor="ipAssignment" className="relative inline-flex items-center cursor-pointer">
                  <input type="checkbox" id="ipAssignment" name="ipAssignment" checked={formData.ipAssignment} onChange={handleCheckboxChange} className="sr-only peer" />
                  <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-focus:ring-4 peer-focus:ring-blue-300 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
            </div>

            <div className="pt-6 text-center">
              <Button
                type="submit"
                disabled={loading}
                className="w-full max-w-xs mx-auto bg-gradient-to-r from-purple-500 via-pink-500 to-red-500 text-white font-semibold py-2 px-4 rounded-2xl shadow-lg hover:from-purple-600 hover:via-pink-600 hover:to-red-600 transition-all duration-300 ease-in-out transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? 'Posting...' : 'Post Job'}
              </Button>
            </div>

          </form>
        </motion.div>
      </div>
    </>
  );
}


