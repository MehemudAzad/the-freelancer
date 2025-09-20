'use client';

import React from 'react';
import Link from 'next/link';
import { Search, Users, Briefcase, Star, ArrowRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { motion } from 'framer-motion';

export default function Home() {
  return (
    <div className="min-h-screen">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-blue-600 to-purple-700 text-white py-24 relative overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center relative z-10">
          <motion.h1
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.8 }}
            className="text-4xl md:text-6xl font-extrabold leading-tight mb-6"
          >
            Find the Perfect{' '}
            <span className="text-yellow-300 drop-shadow-md">Freelancer</span>
            <br /> for Your Project
          </motion.h1>
          <motion.p
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 1 }}
            className="text-lg md:text-2xl mb-10 text-blue-100 max-w-3xl mx-auto leading-relaxed"
          >
            Connect with skilled professionals worldwide. Get your projects done faster and better.
          </motion.p>

          {/* Search Bar */}
          <div className="max-w-4xl mx-auto mb-8">
            <div className="flex flex-col md:flex-row gap-4 bg-white rounded-xl p-2 shadow-xl hover:shadow-2xl transition-all duration-300">
              <div className="flex-1">
                <Input
                  type="text"
                  placeholder="What service are you looking for?"
                  className="border-0 text-gray-900 text-lg focus:ring-0"
                />
              </div>
              <Button size="lg" className="bg-green-600 hover:bg-green-700 text-white px-8 rounded-lg transition-all">
                <Search className="mr-2 h-5 w-5" />
                Search
              </Button>
            </div>
          </div>

          {/* Popular Searches */}
          <div className="flex flex-wrap justify-center gap-2 mb-8 text-sm">
            <span className="text-blue-200 font-medium">Popular:</span>
            {['Web Design', 'Logo Design', 'WordPress', 'Data Entry', 'Translation'].map((term) => (
              <Link
                key={term}
                href={`/search?q=${encodeURIComponent(term)}`}
                className="bg-blue-500 hover:bg-blue-400 px-4 py-1 rounded-full text-white transition-colors shadow-sm"
              >
                {term}
              </Link>
            ))}
          </div>

          {/* CTA Buttons */}
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Button asChild size="lg" variant="secondary" className="rounded-lg shadow hover:shadow-lg transition">
              <Link href="/gigs">
                Browse Services <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
            <Button asChild size="lg" variant="outline" className="rounded-lg border-white hover:bg-white hover:text-blue-700 transition">
              <Link href="/jobs">
                Find Work <ArrowRight className="ml-2 h-5 w-5" />
              </Link>
            </Button>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 grid grid-cols-1 md:grid-cols-4 gap-10 text-center">
          {[
            { value: '10K+', label: 'Active Freelancers', color: 'text-blue-600' },
            { value: '5K+', label: 'Projects Completed', color: 'text-green-600' },
            { value: '98%', label: 'Client Satisfaction', color: 'text-purple-600' },
            { value: '24/7', label: 'Support Available', color: 'text-orange-600' },
          ].map((stat, idx) => (
            <motion.div
              key={idx}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true }}
              transition={{ duration: 0.5, delay: idx * 0.2 }}
              className="p-4"
            >
              <div className={`text-3xl font-bold mb-2 ${stat.color}`}>{stat.value}</div>
              <div className="text-gray-600">{stat.label}</div>
            </motion.div>
          ))}
        </div>
      </section>

      {/* How It Works */}

<section className="py-24 bg-gradient-to-b from-blue-50 via-white to-purple-50">
  <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
    {/* Title */}
    <h2 className="text-4xl md:text-5xl font-bold text-gray-900 mb-4">
      How It Works
    </h2>
    <p className="text-lg text-gray-600 mb-16 max-w-2xl mx-auto">
      Three simple steps to find and work with the best freelancers.
    </p>

    {/* Steps Grid */}
    <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
      {[
        {
          title: "1. Search & Discover",
          icon: <Search className="h-7 w-7 text-blue-600" />,
          desc: "Browse a vast network of talented freelancers tailored to your needs.",
        },
        {
          title: "2. Connect & Hire",
          icon: <Users className="h-7 w-7 text-green-600" />,
          desc: "Review profiles, chat with candidates, and choose the best fit quickly.",
        },
        {
          title: "3. Get Work Done",
          icon: <Briefcase className="h-7 w-7 text-purple-600" />,
          desc: "Collaborate efficiently and bring your projects to life with ease.",
        },
      ].map((step, idx) => (
        <motion.div
          key={idx}
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          viewport={{ once: true }}
          transition={{ duration: 0.5, delay: idx * 0.2 }}
          className="p-8 bg-white/80 border border-gray-100 backdrop-blur-sm rounded-xl shadow-md hover:shadow-xl transition duration-300"
        >
          <div className="w-14 h-14 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-5">
            {step.icon}
          </div>
          <h3 className="text-xl font-semibold text-gray-900 mb-3">{step.title}</h3>
          <p className="text-gray-600 leading-relaxed">{step.desc}</p>
        </motion.div>
      ))}
    </div>
  </div>
</section>


      {/* Popular Categories */}
      <section className="py-20 bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">Popular Categories</h2>
          <p className="text-lg text-gray-600 mb-12">Explore the most in-demand freelance services</p>
          <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-6">
            {[
              'Web Development',
              'Mobile Apps',
              'Graphic Design',
              'Digital Marketing',
              'Writing & Translation',
              'Video & Animation',
              'Music & Audio',
              'Data Science',
              'Business',
              'Photography',
              'AI Services',
              'Consulting'
            ].map((category, idx) => (
              <motion.div
                key={category}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: idx * 0.1 }}
                className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow text-center group"
              >
                <Link href={`/gigs?category=${encodeURIComponent(category)}`}>
                  <div className="text-gray-700 group-hover:text-blue-600 transition-colors font-medium">
                    {category}
                  </div>
                </Link>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="py-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-900 mb-12">What Our Users Say</h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {[
              { name: 'Sarah Johnson', role: 'Startup Founder', content: 'Amazing developer brought our vision to life.', rating: 5 },
              { name: 'Mike Chen', role: 'Digital Marketer', content: 'This platform connected me with great clients.', rating: 5 },
              { name: 'Emily Rodriguez', role: 'E-commerce Owner', content: 'Found specialists for every aspect of our business.', rating: 5 }
            ].map((testimonial, idx) => (
              <motion.div
                key={idx}
                initial={{ opacity: 0, y: 20 }}
                whileInView={{ opacity: 1, y: 0 }}
                viewport={{ once: true }}
                transition={{ duration: 0.5, delay: idx * 0.2 }}
                className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition"
              >
                <div className="flex justify-center mb-4">
                  {[...Array(testimonial.rating)].map((_, i) => (
                    <Star key={i} className="h-5 w-5 text-yellow-400 fill-current" />
                  ))}
                </div>
                <p className="text-gray-600 mb-4">"{testimonial.content}"</p>
                <div>
                  <div className="font-medium text-gray-900">{testimonial.name}</div>
                  <div className="text-sm text-gray-500">{testimonial.role}</div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="py-20 bg-blue-600 text-white text-center">
        <h2 className="text-3xl md:text-4xl font-bold mb-6">Ready to Get Started?</h2>
        <p className="text-lg mb-8 text-blue-100">Join thousands who trust our platform</p>
        <div className="flex flex-col sm:flex-row gap-4 justify-center">
          <Button asChild size="lg" variant="secondary" className="rounded-lg">
            <Link href="/auth/signup">Start as a Client</Link>
          </Button>
          <Button asChild size="lg" variant="outline" className="rounded-lg border-white hover:bg-white hover:text-blue-700">
            <Link href="/auth/signup?type=freelancer">Become a Freelancer</Link>
          </Button>
        </div>
      </section>
    </div>
  );
}
