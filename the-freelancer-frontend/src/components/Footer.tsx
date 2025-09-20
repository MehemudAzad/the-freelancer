import React from 'react';
import Link from 'next/link';
import { Briefcase, Mail, Phone, MapPin } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Company Info */}
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center space-x-2 mb-4">
              <Briefcase className="h-8 w-8 text-blue-400" />
              <span className="text-xl font-bold">FreelanceHub</span>
            </div>
            <p className="text-gray-300 mb-4 max-w-md">
              Connect with top freelancers worldwide. Find the perfect talent for your project or 
              showcase your skills to clients looking for expertise.
            </p>
            <div className="flex space-x-4">
              <div className="flex items-center text-gray-300">
                <Mail className="h-4 w-4 mr-2" />
                <span className="text-sm">hello@freelancehub.com</span>
              </div>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">For Clients</h3>
            <ul className="space-y-2">
              <li>
                <Link href="/jobs" className="text-gray-300 hover:text-white transition-colors">
                  Browse Jobs
                </Link>
              </li>
              <li>
                <Link href="/freelancers" className="text-gray-300 hover:text-white transition-colors">
                  Find Freelancers
                </Link>
              </li>
              <li>
                <Link href="/dashboard/jobs/create" className="text-gray-300 hover:text-white transition-colors">
                  Post a Job
                </Link>
              </li>
              <li>
                <Link href="/how-it-works/clients" className="text-gray-300 hover:text-white transition-colors">
                  How it Works
                </Link>
              </li>
            </ul>
          </div>

          {/* Freelancer Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">For Freelancers</h3>
            <ul className="space-y-2">
              <li>
                <Link href="/gigs" className="text-gray-300 hover:text-white transition-colors">
                  Browse Services
                </Link>
              </li>
              <li>
                <Link href="/dashboard/gigs/create" className="text-gray-300 hover:text-white transition-colors">
                  Create a Gig
                </Link>
              </li>
              <li>
                <Link href="/jobs" className="text-gray-300 hover:text-white transition-colors">
                  Find Work
                </Link>
              </li>
              <li>
                <Link href="/how-it-works/freelancers" className="text-gray-300 hover:text-white transition-colors">
                  How it Works
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom section */}
        <div className="border-t border-gray-800 mt-8 pt-8 flex flex-col md:flex-row justify-between items-center">
          <div className="text-gray-300 text-sm">
            © 2025 FreelanceHub. All rights reserved.
          </div>
          <div className="flex space-x-6 mt-4 md:mt-0">
            <Link href="/privacy" className="text-gray-300 hover:text-white text-sm transition-colors">
              Privacy Policy
            </Link>
            <Link href="/terms" className="text-gray-300 hover:text-white text-sm transition-colors">
              Terms of Service
            </Link>
            <Link href="/support" className="text-gray-300 hover:text-white text-sm transition-colors">
              Support
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
