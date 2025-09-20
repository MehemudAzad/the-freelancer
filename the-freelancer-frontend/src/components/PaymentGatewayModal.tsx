'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  X, 
  CreditCard, 
  Shield, 
  Lock, 
  CheckCircle, 
  AlertCircle, 
  Eye, 
  EyeOff,
  Calendar,
  User,
  DollarSign,
  Zap,
  Sparkles,
  Star,
  Globe
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import paymentService from '@/services/payment';

// Visa Logo Component
const VisaLogo = () => (
  <svg width="64" height="20" viewBox="0 0 64 20" fill="none">
    <rect width="64" height="20" fill="transparent"/>
    <path d="M25.5 2.5L20.1 17.5h-4.2L12.8 7.3c-.2-.8-.4-1.1-.9-1.4-.8-.5-2.2-1-3.4-1.3L8.7 2.5h7c.9 0 1.7.6 1.9 1.6l1.7 9.1L23.6 2.5h4.9zm18.8 10.7c0-4.3-5.9-4.5-5.9-6.4 0-.6.6-1.2 1.8-1.4 1.2-.1 3.2-.2 5.5 1l1-4.6c-1.3-.5-3-1-5.1-1-5.4 0-9.2 2.9-9.2 7 0 3.1 2.7 4.8 4.8 5.8 2.1 1 2.8 1.7 2.8 2.6 0 1.4-1.7 2-3.2 2-2.7 0-4.2-.7-5.4-1.3l-1 4.8c1.2.6 3.5 1.1 5.8 1.1 5.7 0 9.4-2.8 9.4-7.1l-.3-.5zm14.5 7.3h4.3L59.4 2.5h-3.9c-.8 0-1.5.5-1.8 1.2L46.9 17.5h5.4l1.1-3h6.6l.6 3zm-5.7-7l2.7-7.4L57.6 13.5h-4.5zm-29.5-11L18.1 17.5h-5.2L17.4 2.5h4.7z" 
          fill="white"/>
  </svg>
);

// Mastercard Logo Component  
const MastercardLogo = () => (
  <svg width="48" height="30" viewBox="0 0 48 30" fill="none">
    <circle cx="15" cy="15" r="13" fill="#EB001B"/>
    <circle cx="33" cy="15" r="13" fill="#F79E1B"/>
    <path d="M24 6.5c2.8 2.4 4.5 6 4.5 10.5s-1.7 8.1-4.5 10.5c-2.8-2.4-4.5-6-4.5-10.5s1.7-8.1 4.5-10.5z" 
          fill="#FF5F00"/>
    <text x="24" y="25" textAnchor="middle" fontSize="3" fontWeight="bold" fill="white">mastercard</text>
  </svg>
);

// American Express Logo Component
const AmexLogo = () => (
  <svg width="60" height="20" viewBox="0 0 60 20" fill="none" className="text-white">
    <rect width="60" height="20" rx="4" fill="currentColor"/>
    <text x="30" y="14" textAnchor="middle" fontSize="8" fontWeight="bold" fill="#003366">AMEX</text>
  </svg>
);

// Discover Logo Component
const DiscoverLogo = () => (
  <svg width="60" height="20" viewBox="0 0 60 20" fill="none" className="text-white">
    <rect width="60" height="20" rx="4" fill="currentColor"/>
    <text x="30" y="14" textAnchor="middle" fontSize="6" fontWeight="bold" fill="#f47216">DISCOVER</text>
  </svg>
);

interface PaymentGatewayModalProps {
  isOpen: boolean;
  onClose: () => void;
  onPaymentSuccess: () => void;
  proposal: {
    id: number;
    proposedRate: number;
    freelancerInfo?: { name: string };
    deliveryDays: number;
  };
  job: {
    id: string | number;
    title: string;
    currency?: string;
  };
}

interface CardType {
  type: 'visa' | 'mastercard' | 'amex' | 'discover' | 'unknown';
  icon: React.ReactNode;
  background: string;
  textColor: string;
  brand: string;
}

const PaymentGatewayModal: React.FC<PaymentGatewayModalProps> = ({
  isOpen,
  onClose,
  onPaymentSuccess,
  proposal,
  job
}) => {
  const [step, setStep] = useState<'payment' | 'processing' | 'success'>('payment');
  const [cardNumber, setCardNumber] = useState('');
  const [expiryDate, setExpiryDate] = useState('');
  const [cvv, setCvv] = useState('');
  const [cardholderName, setCardholderName] = useState('');
  const [showCvv, setShowCvv] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isProcessing, setIsProcessing] = useState(false);

  // Reset form when modal opens
  useEffect(() => {
    if (isOpen) {
      setStep('payment');
      setCardNumber('');
      setExpiryDate('');
      setCvv('');
      setCardholderName('');
      setErrors({});
      setIsProcessing(false);
    }
  }, [isOpen]);

  // Detect card type
  const detectCardType = (number: string): CardType => {
    const cleaned = number.replace(/\s/g, '');
    
    if (/^4/.test(cleaned)) {
      return { 
        type: 'visa', 
        icon: <VisaLogo />, 
        background: 'linear-gradient(135deg, #1e3a8a 0%, #1e40af 50%, #2563eb 100%)',
        textColor: 'text-white',
        brand: 'VISA'
      };
    } else if (/^5[1-5]/.test(cleaned) || /^2[2-7]/.test(cleaned)) {
      return { 
        type: 'mastercard', 
        icon: <MastercardLogo />, 
        background: 'linear-gradient(135deg, #000000 0%, #1a1a1a 50%, #2d2d2d 100%)',
        textColor: 'text-white',
        brand: 'MASTERCARD'
      };
    } else if (/^3[47]/.test(cleaned)) {
      return { 
        type: 'amex', 
        icon: <AmexLogo />, 
        background: 'linear-gradient(135deg, #006fcf 0%, #003d82 100%)',
        textColor: 'text-white',
        brand: 'AMERICAN EXPRESS'
        
      };
    } else if (/^6/.test(cleaned)) {
      return { 
        type: 'discover', 
        icon: <DiscoverLogo />, 
        background: 'linear-gradient(135deg, #f47216 0%, #d85a00 100%)',
        textColor: 'text-white',
        brand: 'DISCOVER'
      };
    }
    
    return { 
      type: 'unknown', 
      icon: <CreditCard className="w-8 h-8" />, 
      background: 'linear-gradient(135deg, #6b7280 0%, #374151 100%)',
      textColor: 'text-white',
      brand: 'CARD'
    };
  };

  const cardType = detectCardType(cardNumber);

  // Format card number with spaces
  const formatCardNumber = (value: string) => {
    const cleaned = value.replace(/\s/g, '');
    const formatted = cleaned.replace(/(.{4})/g, '$1 ').trim();
    return formatted.substring(0, 19); // Max 16 digits + 3 spaces
  };

  // Format expiry date
  const formatExpiryDate = (value: string) => {
    const cleaned = value.replace(/\D/g, '');
    if (cleaned.length >= 2) {
      return cleaned.substring(0, 2) + '/' + cleaned.substring(2, 4);
    }
    return cleaned;
  };

  // Validation
  const validateForm = () => {
    const newErrors: Record<string, string> = {};
    
    if (!cardNumber.replace(/\s/g, '') || cardNumber.replace(/\s/g, '').length < 13) {
      newErrors.cardNumber = 'Please enter a valid card number';
    }
    
    if (!expiryDate || expiryDate.length < 5) {
      newErrors.expiryDate = 'Please enter expiry date (MM/YY)';
    }
    
    if (!cvv || cvv.length < 3) {
      newErrors.cvv = 'Please enter CVV';
    }
    
    if (!cardholderName.trim()) {
      newErrors.cardholderName = 'Please enter cardholder name';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Handle payment processing
  const handlePayment = async () => {
    if (!validateForm()) return;
    
    setIsProcessing(true);
    setStep('processing');
    
    try {
      // Call the actual payment API using the structure from Postman
      const paymentData = {
        jobId: Number(job.id), // Use actual job ID, converted to number
        amountCents: totalAmount * 100, // Convert to cents
        currency: "usd",
        paymentMethodId: `pm_card_${cardType.type}`,
        clientEmail: "mehemudazad.km@gmail.com", // This should come from user data
        clientName: cardholderName || "Mehemud Azad", // From Postman example
        description: `${job.title}` // Job title as description
      };

      // Make the actual API call
      await paymentService.fundEscrow(paymentData);
      
      setStep('success');
      
      // After showing success, proceed with hiring
      setTimeout(() => {
        onPaymentSuccess();
      }, 2000);
      
    } catch (error) {
      console.error('Payment failed:', error);
      // Handle payment error
      setStep('payment');
      setIsProcessing(false);
      // Show error toast or message
    }
  };

  // Calculate fees
  const projectCost = proposal.proposedRate;
  const platformFee = Math.round(projectCost * 0.03); // 3% platform fee
  const paymentProcessingFee = Math.round(projectCost * 0.029 + 30); // 2.9% + $0.30
  const totalAmount = projectCost + platformFee + paymentProcessingFee;

  if (!isOpen) return null;

  return (
    <AnimatePresence>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        exit={{ opacity: 0 }}
        className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4"
        onClick={onClose}
      >
        <motion.div
          initial={{ opacity: 0, scale: 0.9, y: 20 }}
          animate={{ opacity: 1, scale: 1, y: 0 }}
          exit={{ opacity: 0, scale: 0.9, y: 20 }}
          transition={{ type: "spring", duration: 0.5 }}
          className="bg-white rounded-3xl shadow-2xl max-w-md w-full max-h-[90vh] overflow-y-auto"
          onClick={(e) => e.stopPropagation()}
        >
          {step === 'payment' && (
            <>
              {/* Header */}
              <div className="relative p-6 bg-gradient-to-r from-purple-600 via-blue-600 to-indigo-600 text-white rounded-t-3xl">
                <button
                  onClick={onClose}
                  className="absolute top-4 right-4 p-2 hover:bg-white/20 rounded-full transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
                
                <div className="text-center">
                  <motion.div
                    initial={{ scale: 0 }}
                    animate={{ scale: 1 }}
                    transition={{ delay: 0.2, type: "spring" }}
                    className="w-16 h-16 bg-white/20 rounded-2xl flex items-center justify-center mx-auto mb-4"
                  >
                    <CreditCard className="w-8 h-8" />
                  </motion.div>
                  <h2 className="text-2xl font-bold mb-2">Secure Payment</h2>
                  <p className="text-blue-100">Hire {proposal.freelancerInfo?.name}</p>
                </div>
              </div>

              {/* Payment Form */}
              <div className="p-6">
                {/* Project Summary */}
                <div className="bg-gradient-to-r from-gray-50 to-blue-50 rounded-2xl p-4 mb-6">
                  <h3 className="font-semibold text-gray-900 mb-2" style={{ color: '#111827' }}>{job.title}</h3>
                  <div className="space-y-2 text-sm">
                    <div className="flex justify-between">
                      <span className="text-gray-600" style={{ color: '#4b5563' }}>Project Cost</span>
                      <span className="font-medium text-gray-900" style={{ color: '#111827' }}>${projectCost}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600" style={{ color: '#4b5563' }}>Platform Fee (3%)</span>
                      <span className="font-medium text-gray-900" style={{ color: '#111827' }}>${platformFee}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600" style={{ color: '#4b5563' }}>Payment Processing</span>
                      <span className="font-medium text-gray-900" style={{ color: '#111827' }}>${paymentProcessingFee}</span>
                    </div>
                    <div className="border-t border-gray-200 pt-2 flex justify-between font-bold text-lg">
                      <span className="text-gray-900" style={{ color: '#111827' }}>Total</span>
                      <span className="text-green-600" style={{ color: '#059669' }}>${totalAmount}</span>
                    </div>
                  </div>
                </div>

                {/* Card Display */}
                <div className="mb-6">
                  <div 
                    className={`relative w-full h-48 rounded-2xl p-6 shadow-xl transform perspective-1000 ${cardType.textColor}`}
                    style={{ background: cardType.background }}
                  >
                    {/* Card Brand Logo */}
                    <div className="absolute top-4 right-4">
                      {cardType.icon}
                    </div>
                    
                    {/* EMV Chip */}
                    <div className="absolute top-16 left-6">
                      <div className="w-12 h-9 bg-gradient-to-br from-yellow-300 to-yellow-500 rounded-lg shadow-md">
                        <div className="w-full h-full bg-gradient-to-br from-yellow-200 to-yellow-400 rounded-lg p-1">
                          <div className="w-full h-full bg-gradient-to-br from-yellow-300 to-yellow-600 rounded grid grid-cols-3 gap-px">
                            {[...Array(9)].map((_, i) => (
                              <div key={i} className="bg-yellow-800 rounded-sm opacity-50"></div>
                            ))}
                          </div>
                        </div>
                      </div>
                    </div>
                    
                    {/* Card Number */}
                    <div className="absolute bottom-16 left-6 right-6">
                      <div className="text-xl font-mono tracking-wider mb-4 drop-shadow-sm">
                        {cardNumber || '•••• •••• •••• ••••'}
                      </div>
                    </div>
                    
                    {/* Cardholder Info */}
                    <div className="absolute bottom-6 left-6 right-6 flex justify-between items-end">
                      <div className="flex-1 mr-4">
                        <div className="text-xs opacity-70 mb-1 font-medium">CARDHOLDER NAME</div>
                        <div className="font-medium truncate text-sm">
                          {cardholderName || 'YOUR NAME'}
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-xs opacity-70 mb-1 font-medium">EXPIRES</div>
                        <div className="font-medium text-sm">
                          {expiryDate || 'MM/YY'}
                        </div>
                      </div>
                    </div>
                    
                    {/* Contactless Payment Symbol */}
                    <div className="absolute top-4 left-20">
                      <svg width="20" height="16" viewBox="0 0 20 16" fill="none" className="opacity-70">
                        <path d="M3 13C3 10.2 5.2 8 8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        <path d="M3 9C3 7.3 4.3 6 6 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        <path d="M3 5C3 4.4 3.4 4 4 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        <path d="M7 13C7 11.1 8.1 10 10 10" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        <path d="M7 9C7 8.4 7.4 8 8 8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                        <path d="M11 13C11 12.4 11.4 12 12 12" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
                      </svg>
                    </div>

                    {/* Card Type Text - Only for Visa */}
                    {cardType.type === 'visa' && (
                      <div className="absolute bottom-6 left-6 text-left">
                        <div className="text-xs opacity-80 font-bold tracking-wider">
                          DEBIT
                        </div>
                      </div>
                    )}

                    {/* Security Features */}
                    {cardType.type === 'mastercard' && (
                      <div className="absolute bottom-6 left-6 text-left">
                        <div className="text-xs opacity-80 font-medium">
                          WORLD
                        </div>
                      </div>
                    )}
                    
                    {/* Holographic Effect */}
                    <div className="absolute inset-0 rounded-2xl bg-gradient-to-r from-transparent via-white/5 to-transparent transform -skew-x-12 translate-x-full opacity-0 hover:opacity-100 transition-all duration-1000 pointer-events-none"></div>
                    
                    {/* Card Texture */}
                    <div className="absolute inset-0 rounded-2xl opacity-10" 
                         style={{
                           backgroundImage: 'radial-gradient(circle at 20% 80%, rgba(255,255,255,0.1) 0%, transparent 50%), radial-gradient(circle at 80% 20%, rgba(255,255,255,0.1) 0%, transparent 50%)'
                         }}>
                    </div>
                  </div>
                </div>

                {/* Form Fields */}
                <div className="space-y-4">
                  {/* Card Number */}
                  <div>
                    <label className="block text-sm font-medium mb-2" style={{ color: '#111827' }}>
                      Card Number
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        value={cardNumber}
                        onChange={(e) => setCardNumber(formatCardNumber(e.target.value))}
                        placeholder="1234 5678 9012 3456"
                        className={`w-full p-4 border-2 rounded-xl font-mono text-lg ${
                          errors.cardNumber ? 'border-red-300' : 'border-gray-200'
                        } focus:border-blue-500 focus:ring-0 transition-colors`}
                        style={{ color: '#111827', backgroundColor: '#ffffff' }}
                        maxLength={19}
                      />
                      <div className="absolute right-4 top-1/2 transform -translate-y-1/2">
                        <div className="w-8 h-5 flex items-center justify-center">
                          {cardType.type === 'visa' && <div className="text-xs font-bold text-blue-600">VISA</div>}
                          {cardType.type === 'mastercard' && <div className="text-xs font-bold text-red-600">MC</div>}
                          {cardType.type === 'amex' && <div className="text-xs font-bold text-blue-600">AMEX</div>}
                          {cardType.type === 'discover' && <div className="text-xs font-bold text-orange-600">DISC</div>}
                          {cardType.type === 'unknown' && <CreditCard className="w-5 h-5 text-gray-400" />}
                        </div>
                      </div>
                    </div>
                    {errors.cardNumber && (
                      <p className="text-red-500 text-sm mt-1">{errors.cardNumber}</p>
                    )}
                  </div>

                  {/* Expiry and CVV */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2" style={{ color: '#111827' }}>
                        Expiry Date
                      </label>
                      <input
                        type="text"
                        value={expiryDate}
                        onChange={(e) => setExpiryDate(formatExpiryDate(e.target.value))}
                        placeholder="MM/YY"
                        className={`w-full p-4 border-2 rounded-xl font-mono ${
                          errors.expiryDate ? 'border-red-300' : 'border-gray-200'
                        } focus:border-blue-500 focus:ring-0 transition-colors`}
                        style={{ color: '#111827', backgroundColor: '#ffffff' }}
                        maxLength={5}
                      />
                      {errors.expiryDate && (
                        <p className="text-red-500 text-sm mt-1">{errors.expiryDate}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium mb-2" style={{ color: '#111827' }}>
                        CVV
                      </label>
                      <div className="relative">
                        <input
                          type={showCvv ? 'text' : 'password'}
                          value={cvv}
                          onChange={(e) => setCvv(e.target.value.replace(/\D/g, '').substring(0, 4))}
                          placeholder="123"
                          className={`w-full p-4 border-2 rounded-xl font-mono pr-12 ${
                            errors.cvv ? 'border-red-300' : 'border-gray-200'
                          } focus:border-blue-500 focus:ring-0 transition-colors`}
                          style={{ color: '#111827', backgroundColor: '#ffffff' }}
                          maxLength={4}
                        />
                        <button
                          type="button"
                          onClick={() => setShowCvv(!showCvv)}
                          className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                        >
                          {showCvv ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                        </button>
                      </div>
                      {errors.cvv && (
                        <p className="text-red-500 text-sm mt-1">{errors.cvv}</p>
                      )}
                    </div>
                  </div>

                  {/* Cardholder Name */}
                  <div>
                    <label className="block text-sm font-medium mb-2" style={{ color: '#111827' }}>
                      Cardholder Name
                    </label>
                    <input
                      type="text"
                      value={cardholderName}
                      onChange={(e) => setCardholderName(e.target.value.toUpperCase())}
                      placeholder="JOHN DOE"
                      className={`w-full p-4 border-2 rounded-xl ${
                        errors.cardholderName ? 'border-red-300' : 'border-gray-200'
                      } focus:border-blue-500 focus:ring-0 transition-colors`}
                      style={{ color: '#111827', backgroundColor: '#ffffff' }}
                    />
                    {errors.cardholderName && (
                      <p className="text-red-500 text-sm mt-1">{errors.cardholderName}</p>
                    )}
                  </div>
                </div>

                {/* Security Info */}
                <div className="mt-6 p-4 bg-green-50 rounded-xl border border-green-200">
                  <div className="flex items-center space-x-3">
                    <Shield className="w-6 h-6 text-green-600" />
                    <div>
                      <p className="text-sm font-medium" style={{ color: '#065f46' }}>
                        256-bit SSL Encryption
                      </p>
                      <p className="text-xs" style={{ color: '#059669' }}>
                        Your payment information is secure and encrypted
                      </p>
                    </div>
                  </div>
                </div>

                {/* Payment Button */}
                <Button
                  onClick={handlePayment}
                  disabled={isProcessing}
                  className="w-full mt-6 bg-gradient-to-r from-purple-600 to-blue-600 hover:from-purple-700 hover:to-blue-700 text-white p-4 rounded-xl font-semibold text-lg shadow-lg hover:shadow-xl transition-all duration-300"
                >
                  <Lock className="w-5 h-5 mr-2" />
                  Pay ${totalAmount} & Hire Freelancer
                </Button>

                {/* Trust Indicators */}
                <div className="mt-4 flex items-center justify-center space-x-6 text-xs" style={{ color: '#6b7280' }}>
                  <div className="flex items-center space-x-1">
                    <Shield className="w-4 h-4" />
                    <span>SSL Secured</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Globe className="w-4 h-4" />
                    <span>Global</span>
                  </div>
                  <div className="flex items-center space-x-1">
                    <Star className="w-4 h-4" />
                    <span>Trusted</span>
                  </div>
                </div>
              </div>
            </>
          )}

          {step === 'processing' && (
            <div className="p-8 text-center">
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                className="w-20 h-20 bg-gradient-to-r from-blue-500 to-purple-500 rounded-full flex items-center justify-center mx-auto mb-6"
              >
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                >
                  <Zap className="w-10 h-10 text-white" />
                </motion.div>
              </motion.div>
              
              <h3 className="text-2xl font-bold mb-2" style={{ color: '#111827' }}>Processing Payment</h3>
              <p className="mb-6" style={{ color: '#4b5563' }}>Please wait while we securely process your payment...</p>
              
              <div className="space-y-3">
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.5 }}
                  className="flex items-center justify-center space-x-3 text-green-600"
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>Verifying card details</span>
                </motion.div>
                
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 1.5 }}
                  className="flex items-center justify-center space-x-3 text-green-600"
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>Processing payment</span>
                </motion.div>
                
                <motion.div
                  initial={{ opacity: 0, x: -20 }}
                  animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 2.5 }}
                  className="flex items-center justify-center space-x-3 text-green-600"
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>Creating contract</span>
                </motion.div>
              </div>
            </div>
          )}

          {step === 'success' && (
            <div className="p-8 text-center">
              <motion.div
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                transition={{ type: "spring", delay: 0.2 }}
                className="w-20 h-20 bg-gradient-to-r from-green-500 to-emerald-500 rounded-full flex items-center justify-center mx-auto mb-6"
              >
                <CheckCircle className="w-10 h-10 text-white" />
              </motion.div>
              
              <motion.h3
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.4 }}
                className="text-2xl font-bold mb-2"
                style={{ color: '#111827' }}
              >
                Payment Successful!
              </motion.h3>
              
              <motion.p
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.6 }}
                className="mb-6"
                style={{ color: '#4b5563' }}
              >
                Your payment has been processed and the freelancer has been hired.
              </motion.p>
              
              <motion.div
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: 0.8 }}
                className="flex items-center justify-center space-x-2 text-green-600 bg-green-50 rounded-xl p-4"
              >
                <Sparkles className="w-5 h-5" />
                <span className="font-medium">Contract created successfully!</span>
              </motion.div>
            </div>
          )}
        </motion.div>
      </motion.div>
    </AnimatePresence>
  );
};

export default PaymentGatewayModal;