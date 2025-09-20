"use client";
import React, { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import { useAppSelector, useAppDispatch } from "@/store/hooks";
import { loginSuccess } from "@/store/authSlice";
import { getBackendUrl } from "@/config/api";
import { 
  Sparkles, 
  TrendingUp, 
  Zap, 
  Star, 
  Award, 
  Users, 
  Code,
  Palette,
  PenTool,
  Globe,
  Database,
  Camera,
  Filter,
  ArrowLeft,
  DollarSign,
  Clock,
  Tag,
  FileText,
  Briefcase,
  X,
  Check,
  Upload,
  Image
} from 'lucide-react';const CreateGigPage = () => {
  const router = useRouter();
  const dispatch = useAppDispatch();
  const { user, isAuthenticated } = useAppSelector((state) => state.auth);

  const [form, setForm] = useState<{
    title: string;
    description: string;
    category: string;
    tags: string[];
    startingPrice: string;
    deliveryDays: string;
    status:  "ACTIVE" | "INACTIVE" | "DRAFT";
    active: boolean;
  }>({
    title: "",
    description: "",
    category: "",
    tags: [],
    startingPrice: "",
    deliveryDays: "",
    status: "ACTIVE",
    active: true,
  });
  
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [filePreview, setFilePreview] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [authLoading, setAuthLoading] = useState(true);

  // Predefined tags for different categories
  const predefinedTags = [
    "Web Development", "Mobile App", "UI Design", "UX Design", "Logo Design",
    "Branding", "Graphic Design", "WordPress", "React", "JavaScript",
    "CSS", "HTML", "Figma", "Photoshop", "Illustration", "Animation",
    "SEO", "Marketing", "Content Writing", "Copywriting", "Translation",
    "Video Editing", "Photography", "3D Modeling", "Game Development",
    "Database", "API Integration", "E-commerce", "Shopify", "WooCommerce",
    "Social Media", "Digital Marketing", "Email Marketing", "Lead Generation",
    "Data Entry", "Virtual Assistant", "Customer Support", "Project Management"
  ];

  // Categories with icons and gradients
  const categories = [
    { name: 'All Categories', icon: Sparkles, gradient: 'from-purple-400 to-pink-400' },
    { name: 'Web Development', icon: Code, gradient: 'from-blue-400 to-cyan-400' },
    { name: 'Mobile Development', icon: Zap, gradient: 'from-green-400 to-blue-400' },
    { name: 'Graphic Design', icon: Palette, gradient: 'from-pink-400 to-rose-400' },
    { name: 'Digital Marketing', icon: Globe, gradient: 'from-orange-400 to-red-400' },
    { name: 'Writing & Translation', icon: PenTool, gradient: 'from-indigo-400 to-purple-400' },
    { name: 'Video & Animation', icon: Camera, gradient: 'from-yellow-400 to-orange-400' },
    { name: 'Music & Audio', icon: Star, gradient: 'from-teal-400 to-cyan-400' },
    { name: 'Data Science', icon: Database, gradient: 'from-violet-400 to-purple-400' },
    { name: 'Business', icon: Award, gradient: 'from-emerald-400 to-green-400' },
    { name: 'Photography', icon: Camera, gradient: 'from-rose-400 to-pink-400' },
    { name: 'Programming & Tech', icon: Code, gradient: 'from-blue-400 to-indigo-400' },
    { name: 'AI & Machine Learning', icon: TrendingUp, gradient: 'from-purple-400 to-indigo-400' },
    { name: 'Game Development', icon: Users, gradient: 'from-cyan-400 to-blue-400' }
  ];

  useEffect(() => {
    const checkAuth = async () => {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken) {
        router.push("/auth/login");
        return;
      }

      if (!isAuthenticated || !user) {
        try {
          const axios = (await import("axios")).default;
          const response = await axios.get(`${getBackendUrl()}/api/auth/me`, {
            headers: { Authorization: `Bearer ${accessToken}` },
          });

          dispatch(
            loginSuccess({
              user: response.data,
              accessToken: accessToken,
              refreshToken: localStorage.getItem("refreshToken") || "",
            })
          );

          if (response.data.role?.toUpperCase() !== "FREELANCER") {
            router.push("/dashboard");
            return;
          }
        } catch {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          router.push("/auth/login");
          return;
        }
      } else if (user.role?.toUpperCase() !== "FREELANCER") {
        router.push("/dashboard");
        return;
      }

      setAuthLoading(false);
    };

    checkAuth();
  }, [isAuthenticated, user, router, dispatch]);

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleTagSelect = (tag: string) => {
    const currentTags = form.tags as string[];
    if (currentTags.includes(tag)) {
      // Remove tag if already selected
      setForm({ ...form, tags: currentTags.filter(t => t !== tag) });
    } else {
      // Add tag if not selected (limit to 5 tags)
      if (currentTags.length < 5) {
        setForm({ ...form, tags: [...currentTags, tag] });
      }
    }
  };

  const handleTagRemove = (tagToRemove: string) => {
    const currentTags = form.tags as string[];
    setForm({ ...form, tags: currentTags.filter(tag => tag !== tagToRemove) });
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      // Check file size (limit to 10MB)
      if (file.size > 10 * 1024 * 1024) {
        setError("File size should be less than 10MB");
        return;
      }

      // Check file type (images only)
      if (!file.type.startsWith('image/')) {
        setError("Please select an image file");
        return;
      }

      setSelectedFile(file);
      setError("");

      // Create preview
      const reader = new FileReader();
      reader.onload = (e) => {
        setFilePreview(e.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const removeFile = () => {
    setSelectedFile(null);
    setFilePreview(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError("");
    setSuccess("");

    try {
      const axios = (await import("axios")).default;
      const accessToken = localStorage.getItem("accessToken");

      if (!accessToken) {
        setError("You are not authenticated. Please login again.");
        router.push("/auth/login");
        return;
      }

      // Validate required fields
      if (!form.title.trim()) {
        setError("Title is required");
        setLoading(false);
        return;
      }

      if (!form.description.trim()) {
        setError("Description is required");
        setLoading(false);
        return;
      }

      if (!form.category) {
        setError("Category is required");
        setLoading(false);
        return;
      }

      if (!selectedFile) {
        setError("Please upload a gig image");
        setLoading(false);
        return;
      }

      // Prepare the gig data as JSON
      const gigData = {
        title: form.title.trim(),
        description: form.description.trim(),
        category: form.category,
        tags: form.tags,
        startingPrice: Number(form.startingPrice),
        deliveryDays: Number(form.deliveryDays),
        status: form.status,
        active: form.active,
      };

      // Create FormData
      const formData = new FormData();
      const gigBlob = new Blob([JSON.stringify(gigData)], { type: 'application/json' });
    formData.append('gig', gigBlob);
      formData.append('file', selectedFile);

      const response = await axios.post(
        `${getBackendUrl()}/api/gigs/create-with-media`,
        formData,
        {
          headers: {
            Authorization: `Bearer ${accessToken}`,
            // Don't set Content-Type manually - let the browser set it with boundary
          },
        }
      );

      setSuccess("🚀 Gig posted successfully!");
      setTimeout(() => router.push("/dashboard"), 1500);
    } catch (err: any) {
      if (err.response?.status === 401) {
        setError("Session expired. Please login again.");
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        router.push("/auth/login");
      } else {
        setError(err.response?.data?.message || "Failed to post gig.");
      }
    } finally {
      setLoading(false);
    }
  };

  if (authLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-50 via-white to-purple-50">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
          className="w-16 h-16 border-4 border-purple-200 border-t-purple-600 rounded-full"
        />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-50 via-white to-purple-50 py-12 px-4">
      {/* Animated Hero Section */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="max-w-6xl mx-auto"
      >
        {/* Header */}
        <div className="text-center mb-12">
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            className="inline-flex items-center justify-center mb-6"
          >
            <Sparkles className="h-8 w-8 text-purple-500 mr-3" />
            <span className="text-purple-600 font-semibold text-lg">Create Your Service</span>
            <Sparkles className="h-8 w-8 text-purple-500 ml-3" />
          </motion.div>

          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.3 }}
            className="text-4xl md:text-6xl font-bold mb-6"
          >
            <span className="bg-gradient-to-r from-blue-500 to-green-400 bg-clip-text text-transparent">
              Share Your
            </span>
            <span className="bg-gradient-to-r from-purple-600 via-pink-500 to-orange-500 bg-clip-text text-transparent block">
              Amazing Skills
            </span>
          </motion.h1>


          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className="text-xl text-gray-600 max-w-2xl mx-auto leading-relaxed"
          >
            Create a professional gig that showcases your talents and attracts the right clients
          </motion.p>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Categories Sidebar */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6, delay: 0.5 }}
            className="lg:col-span-1"
          >
            <div className="bg-white/90 backdrop-blur-xl rounded-3xl shadow-2xl border border-white/30 p-8 sticky top-8 overflow-hidden">
              {/* Background decoration */}
              <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-bl from-purple-100/50 to-transparent rounded-full -translate-y-8 translate-x-8" />

              <div className="relative z-10">
                <h3 className="font-bold text-2xl flex items-center bg-gradient-to-r from-purple-600 to-pink-600 bg-clip-text text-transparent mb-8">
                  <Filter className="mr-3 h-6 w-6 text-purple-500" />
                  Categories
                </h3>

                <div className="space-y-3">
                  {categories.filter(cat => cat.name !== 'All Categories').map((category, index) => {
                    const IconComponent = category.icon;
                    const isSelected = form.category === category.name;

                    return (
                      <motion.button
                        key={category.name}
                        initial={{ opacity: 0, x: -20 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.4, delay: 0.6 + index * 0.03 }}
                        whileHover={{ scale: 1.02, x: 4 }}
                        whileTap={{ scale: 0.98 }}
                        onClick={() => setForm({ ...form, category: category.name })}
                        className={`w-full text-left px-5 py-4 rounded-2xl transition-all duration-300 group relative overflow-hidden border ${isSelected
                            ? `bg-gradient-to-r ${category.gradient} text-white shadow-xl border-transparent transform scale-[1.02]`
                            : 'text-gray-700 hover:text-white bg-gray-50 hover:bg-gradient-to-r hover:' + category.gradient + ' border-gray-200/50 hover:border-transparent shadow-sm hover:shadow-lg'
                          }`}
                      >
                        {/* Shimmer effect for selected */}
                        {isSelected && (
                          <motion.div
                            animate={{ x: [-100, 300] }}
                            transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                            className="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent skew-x-12"
                          />
                        )}

                        <div className="flex items-center relative z-10">
                          <motion.div
                            animate={{
                              rotate: isSelected ? [0, 10, -10, 0] : 0,
                              scale: isSelected ? [1, 1.1, 1] : 1
                            }}
                            transition={{ duration: 0.5, ease: "easeInOut" }}
                          >
                            <IconComponent className={`h-5 w-5 mr-4 transition-all duration-300 ${isSelected ? 'text-white' : 'text-gray-500 group-hover:text-white'
                              }`} />
                          </motion.div>
                          <span className="font-semibold text-sm">{category.name}</span>

                          {isSelected && (
                            <motion.div
                              initial={{ scale: 0, rotate: 180 }}
                              animate={{ scale: 1, rotate: 0 }}
                              transition={{ duration: 0.3, ease: "backOut" }}
                              className="ml-auto"
                            >
                              <Sparkles className="h-4 w-4 text-white animate-pulse" />
                            </motion.div>
                          )}
                        </div>
                      </motion.button>
                    );
                  })}
                </div>
              </div>
            </div>
          </motion.div>

          {/* Form Section */}
          <motion.div
            initial={{ opacity: 0, y: 30 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.7 }}
            className="lg:col-span-2"
          >
            <div className="bg-white rounded-3xl shadow-2xl border border-gray-100 p-8 overflow-hidden">
              {/* Background decoration */}
              <div className="absolute top-0 left-0 w-24 h-24 bg-gradient-to-br from-blue-100/50 to-transparent rounded-full -translate-y-4 -translate-x-4" />

              <div className="relative z-10">
                <div className="text-center mb-8">
                  <h2 className="text-3xl font-bold bg-gradient-to-r from-gray-900 via-blue-900 to-purple-900 bg-clip-text text-transparent mb-2">
                    Gig Details
                  </h2>
                  <p className="text-gray-600">Tell clients what makes your service special</p>
                </div>

                <form onSubmit={handleSubmit} className="space-y-8">
                  {/* Error/Success Messages */}
                  <AnimatePresence>
                    {error && (
                      <motion.div
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.9 }}
                        className="bg-red-50 border border-red-200 rounded-2xl p-4 text-red-600 text-sm text-center"
                      >
                        {error}
                      </motion.div>
                    )}
                    {success && (
                      <motion.div
                        initial={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        exit={{ opacity: 0, scale: 0.9 }}
                        className="bg-green-50 border border-green-200 rounded-2xl p-4 text-green-600 text-sm text-center"
                      >
                        {success}
                      </motion.div>
                    )}
                  </AnimatePresence>

                  {/* Title Field */}
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.8 }}
                  >
                    <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                      <Briefcase className="h-4 w-4 mr-2 text-purple-500" />
                      Gig Title
                    </label>
                    <Input
                      name="title"
                      type="text"
                      placeholder="I will create a stunning logo design for your business"
                      value={form.title}
                      onChange={handleChange}
                      required
                      className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 placeholder:text-gray-500 focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition-all duration-300"
                    />
                  </motion.div>

                  {/* Description Field */}
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 0.9 }}
                  >
                    <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                      <FileText className="h-4 w-4 mr-2 text-purple-500" />
                      Description
                    </label>
                    <textarea
                      name="description"
                      placeholder="Describe your service in detail. What will you deliver? What makes you different?"
                      value={form.description}
                      onChange={handleChange}
                      required
                      rows={5}
                      className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 placeholder:text-gray-500 focus:outline-none focus:ring-2 focus:ring-purple-400 focus:border-purple-400 transition-all duration-300 resize-none"
                    />
                  </motion.div>

                  {/* Tags Field */}
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 1.0 }}
                  >
                    <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                      <Tag className="h-4 w-4 mr-2 text-purple-500" />
                      Skills & Tags (Select up to 5)
                    </label>
                    
                    {/* Selected Tags Display */}
                    {form.tags.length > 0 && (
                      <div className="mb-4 p-4 bg-purple-50 rounded-2xl border border-purple-100">
                        <div className="flex flex-wrap gap-2">
                          {form.tags.map((tag, index) => (
                            <motion.span
                              key={tag}
                              initial={{ scale: 0 }}
                              animate={{ scale: 1 }}
                              exit={{ scale: 0 }}
                              className="inline-flex items-center px-3 py-1.5 bg-gradient-to-r from-purple-500 to-pink-500 text-white text-sm font-medium rounded-full"
                            >
                              {tag}
                              <button
                                type="button"
                                onClick={() => handleTagRemove(tag)}
                                className="ml-2 hover:bg-white/20 rounded-full p-0.5 transition-colors"
                              >
                                <X className="h-3 w-3" />
                              </button>
                            </motion.span>
                          ))}
                        </div>
                        <p className="text-xs text-purple-600 mt-2">
                          {form.tags.length}/5 tags selected
                        </p>
                      </div>
                    )}

                    {/* Available Tags */}
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2 max-h-48 overflow-y-auto p-4 bg-gray-50 rounded-2xl border border-gray-200">
                      {predefinedTags.map((tag, index) => {
                        const isSelected = form.tags.includes(tag);
                        const isDisabled = !isSelected && form.tags.length >= 5;
                        
                        return (
                          <motion.button
                            key={tag}
                            type="button"
                            onClick={() => !isDisabled && handleTagSelect(tag)}
                            disabled={isDisabled}
                            whileHover={!isDisabled ? { scale: 1.05 } : {}}
                            whileTap={!isDisabled ? { scale: 0.95 } : {}}
                            className={`
                              relative p-2 text-xs font-medium rounded-xl border transition-all duration-200 text-left
                              ${isSelected 
                                ? 'bg-gradient-to-r from-purple-500 to-pink-500 text-white border-purple-300 shadow-lg' 
                                : isDisabled 
                                ? 'bg-gray-200 text-gray-400 border-gray-200 cursor-not-allowed'
                                : 'bg-white text-gray-700 border-gray-200 hover:border-purple-300 hover:bg-purple-50'
                              }
                            `}
                          >
                            <div className="flex items-center justify-between">
                              <span className="truncate">{tag}</span>
                              {isSelected && (
                                <motion.div
                                  initial={{ scale: 0, rotate: 180 }}
                                  animate={{ scale: 1, rotate: 0 }}
                                  className="ml-1"
                                >
                                  <Check className="h-3 w-3" />
                                </motion.div>
                              )}
                            </div>
                          </motion.button>
                        );
                      })}
                    </div>
                    
                    <p className="text-xs text-gray-500 mt-2">
                      Select relevant skills and technologies for your gig to help clients find you
                    </p>
                  </motion.div>

                  {/* File Upload Section */}
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 1.0 }}
                  >
                    <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                      <Image className="h-4 w-4 mr-2 text-indigo-500" />
                      Gig Image *
                    </label>
                    
                    {!filePreview ? (
                      <div className="relative">
                        <input
                          type="file"
                          accept="image/*"
                          onChange={handleFileChange}
                          className="hidden"
                          id="file-upload"
                        />
                        <label
                          htmlFor="file-upload"
                          className="flex flex-col items-center justify-center w-full h-32 border-2 border-gray-300 border-dashed rounded-2xl cursor-pointer bg-gray-50 hover:bg-gray-100 transition-all duration-300 hover:border-indigo-400"
                        >
                          <div className="flex flex-col items-center justify-center pt-5 pb-6">
                            <Upload className="w-8 h-8 mb-2 text-gray-500" />
                            <p className="mb-2 text-sm text-gray-500">
                              <span className="font-semibold">Click to upload</span> or drag and drop
                            </p>
                            <p className="text-xs text-gray-500">PNG, JPG, JPEG (MAX. 10MB)</p>
                          </div>
                        </label>
                      </div>
                    ) : (
                      <div className="relative">
                        <div className="w-full h-48 rounded-2xl overflow-hidden shadow-lg">
                          <img
                            src={filePreview}
                            alt="Preview"
                            className="w-full h-full object-cover"
                          />
                        </div>
                        <button
                          type="button"
                          onClick={removeFile}
                          className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors duration-200"
                        >
                          <X className="w-4 h-4" />
                        </button>
                      </div>
                    )}
                  </motion.div>

                  {/* Price and Delivery Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: 1.1 }}
                    >
                      <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                        <DollarSign className="h-4 w-4 mr-2 text-green-500" />
                        Starting Price ($)
                      </label>
                      <Input
                        name="startingPrice"
                        type="number"
                        placeholder="25"
                        value={form.startingPrice}
                        onChange={handleChange}
                        required
                        min={5}
                        className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 placeholder:text-gray-500 focus:ring-2 focus:ring-green-400 focus:border-green-400 transition-all duration-300"
                      />
                    </motion.div>

                    <motion.div
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: 1.2 }}
                    >
                      <label className="flex items-center text-sm font-semibold text-gray-700 mb-3">
                        <Clock className="h-4 w-4 mr-2 text-blue-500" />
                        Delivery Days
                      </label>
                      <Input
                        name="deliveryDays"
                        type="number"
                        placeholder="3"
                        value={form.deliveryDays}
                        onChange={handleChange}
                        required
                        min={1}
                        className="w-full px-4 py-3 rounded-2xl border border-gray-200 bg-white text-gray-900 placeholder:text-gray-500 focus:ring-2 focus:ring-blue-400 focus:border-blue-400 transition-all duration-300"
                      />
                    </motion.div>
                  </div>

                  {/* Submit Button */}
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.4, delay: 1.3 }}
                    className="pt-6"
                  >
                    <Button
                      type="submit"
                      disabled={loading || !form.category || !selectedFile}
                      className="w-full bg-gradient-to-r from-purple-500 via-pink-500 to-orange-500 hover:from-purple-600 hover:via-pink-600 hover:to-orange-600 text-white font-bold py-4 px-8 rounded-2xl text-lg shadow-2xl hover:shadow-3xl transition-all duration-300 transform hover:scale-[1.02] disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {loading ? (
                        <div className="flex items-center justify-center">
                          <motion.div
                            animate={{ rotate: 360 }}
                            transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                            className="w-5 h-5 border-2 border-white border-t-transparent rounded-full mr-3"
                          />
                          Creating Gig...
                        </div>
                      ) : (
                        <div className="flex items-center justify-center">
                          <Sparkles className="h-5 w-5 mr-2" />
                          Create Amazing Gig
                        </div>
                      )}
                    </Button>
                  </motion.div>

                  {/* Back Button */}
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    transition={{ duration: 0.4, delay: 1.4 }}
                    className="text-center pt-4"
                  >
                    <button
                      type="button"
                      onClick={() => router.push("/dashboard")}
                      className="inline-flex items-center text-gray-500 hover:text-purple-600 font-medium transition-colors duration-300"
                    >
                      <ArrowLeft className="h-4 w-4 mr-2" />
                      Back to Dashboard
                    </button>
                  </motion.div>
                </form>
              </div>
            </div>
          </motion.div>
        </div>
      </motion.div>
    </div>
  );
};

export default CreateGigPage;
