import { useState, useEffect, useRef } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Send, ShieldAlert, CheckCircle2, XCircle, Calendar, Clock,
  MapPin, MessageSquare, AlertCircle, ArrowLeft, RefreshCw, Lock
} from 'lucide-react'
import { chatApi } from '@/api/chat'
import { bookingApi } from '@/api/booking'
import { formatPrice, formatDate, formatTime } from '@/utils/formatters'
import type { Conversation, Message, Quote } from '@/types'

export default function CustomerChat() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const qc = useQueryClient()

  const initialProviderId = searchParams.get('providerId') ? Number(searchParams.get('providerId')) : null
  const initialServiceId = searchParams.get('serviceId') ? Number(searchParams.get('serviceId')) : null

  const [activeConvId, setActiveConvId] = useState<number | null>(null)
  const [messageText, setMessageText] = useState('')
  const [bookingModalQuote, setBookingModalQuote] = useState<Quote | null>(null)

  // Booking form states
  const [bookingDate, setBookingDate] = useState('')
  const [bookingTime, setBookingTime] = useState('10:00')
  const [serviceAddress, setServiceAddress] = useState('')
  const [notes, setNotes] = useState('')

  const messagesEndRef = useRef<HTMLDivElement>(null)

  // Fetch all conversations
  const { data: conversations, isLoading: loadingConvs, refetch: refetchConvs } = useQuery({
    queryKey: ['chat', 'conversations'],
    queryFn: () => chatApi.getConversations(),
    select: (r) => r.data,
    refetchInterval: 4000,
  })

  // Auto-select or create conversation when providerId is passed in URL
  useEffect(() => {
    if (initialProviderId && conversations) {
      const existing = initialServiceId
        ? conversations.find((c) => c.providerId === initialProviderId && c.catalogItemId === initialServiceId)
        : conversations.find((c) => c.providerId === initialProviderId)
      if (existing) {
        setActiveConvId(existing.id)
      } else if (!activeConvId) {
        // Trigger creation of new conversation
        chatApi.createConversation({
          providerId: initialProviderId,
          catalogItemId: initialServiceId ?? undefined,
        }).then((res) => {
          qc.invalidateQueries({ queryKey: ['chat', 'conversations'] })
          setActiveConvId(res.data.id)
        }).catch(() => {
          // Handled or exists
        })
      }
    } else if (!activeConvId && conversations && conversations.length > 0) {
      setActiveConvId(conversations[0].id)
    }
  }, [initialProviderId, initialServiceId, conversations, activeConvId, qc])

  // Fetch active conversation details
  const { data: activeConv, isLoading: loadingActive, isError: activeConvError, refetch: refetchActive } = useQuery({
    queryKey: ['chat', 'conversation', activeConvId],
    queryFn: () => chatApi.getConversation(activeConvId!),
    select: (r) => r.data,
    enabled: !!activeConvId,
    refetchInterval: 3000,
  })

  // Scroll to bottom when messages update
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [activeConv?.messages])

  // Send message mutation
  const sendMutation = useMutation({
    mutationFn: (text: string) => {
      if (!activeConvId) throw new Error('No active conversation')
      return chatApi.sendMessage(activeConvId, { message: text })
    },
    onSuccess: () => {
      setMessageText('')
      qc.invalidateQueries({ queryKey: ['chat', 'conversation', activeConvId] })
      qc.invalidateQueries({ queryKey: ['chat', 'conversations'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Failed to send message')
    },
  })

  // Accept quote mutation
  const acceptQuoteMutation = useMutation({
    mutationFn: (quoteId: number) => chatApi.acceptQuote(quoteId),
    onSuccess: (res) => {
      toast.success('Quote accepted! You can now complete your booking with the agreed price.')
      qc.invalidateQueries({ queryKey: ['chat', 'conversation', activeConvId] })
      qc.invalidateQueries({ queryKey: ['chat', 'conversations'] })
      setBookingModalQuote(res.data)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Failed to accept quote')
    },
  })

  // Decline quote mutation
  const declineQuoteMutation = useMutation({
    mutationFn: (quoteId: number) => chatApi.declineQuote(quoteId),
    onSuccess: () => {
      toast.info('Quote declined')
      qc.invalidateQueries({ queryKey: ['chat', 'conversation', activeConvId] })
      qc.invalidateQueries({ queryKey: ['chat', 'conversations'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Failed to decline quote')
    },
  })

  // Book with agreed quote mutation
  const bookAgreedMutation = useMutation({
    mutationFn: async () => {
      if (!bookingModalQuote) throw new Error('No quote selected')
      if (!bookingDate) throw new Error('Please select a booking date')
      if (!serviceAddress.trim()) throw new Error('Please enter the service address')

      const timeStr = bookingTime ? (bookingTime.length === 5 ? `${bookingTime}:00` : bookingTime) : '10:00:00'
      const requestedStartAt = `${bookingDate}T${timeStr}Z`

      return bookingApi.create({
        providerId: bookingModalQuote.providerId,
        catalogItemId: bookingModalQuote.catalogItemId ?? 0,
        quoteId: bookingModalQuote.id,
        description: `${bookingModalQuote.description}${notes ? ` | Notes: ${notes}` : ''}`,
        serviceAddress: serviceAddress.trim(),
        latitude: 11.1085,
        longitude: 77.3411,
        requestedStartAt,
      })
    },
    onSuccess: (res) => {
      toast.success('Booking confirmed at agreed quote price!')
      setBookingModalQuote(null)
      navigate(`/customer/bookings/${res.data.id}`)
    },
    onError: (err: any) => {
      const msg = err?.response?.data?.message || err?.message || 'Failed to complete booking'
      toast.error(msg)
    },
  })

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault()
    if (!messageText.trim() || sendMutation.isPending) return
    sendMutation.mutate(messageText.trim())
  }

  return (
    <div className="max-w-6xl mx-auto space-y-4">
      {/* Header Banner */}
      <div className="bg-white rounded-2xl p-4 sm:p-5 border border-slate-200/80 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-slate-900 flex items-center gap-2">
            <MessageSquare className="w-6 h-6 text-blue-600" />
            Messages & Official Quotes
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Chat directly with verified service providers, request custom quotes, and book at agreed prices.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
            <ShieldAlert className="w-3.5 h-3.5 text-emerald-600" />
            Chat Safety Active
          </span>
          <button
            onClick={() => { refetchConvs(); if (activeConvId) refetchActive(); }}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition-colors"
            title="Refresh"
          >
            <RefreshCw className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Main Split Layout */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-sm overflow-hidden grid grid-cols-1 md:grid-cols-12 min-h-[620px]">
        {/* Left Panel: Conversation List */}
        <div className={`md:col-span-4 border-r border-slate-200/80 flex flex-col ${activeConvId ? 'hidden md:flex' : 'flex'}`}>
          <div className="p-4 border-b border-slate-100 bg-slate-50/50">
            <h2 className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Active Conversations ({conversations?.length ?? 0})
            </h2>
          </div>

          <div className="flex-1 overflow-y-auto divide-y divide-slate-100">
            {loadingConvs ? (
              <div className="p-6 text-center text-sm text-slate-400">Loading conversations...</div>
            ) : !conversations || conversations.length === 0 ? (
              <div className="p-6 text-center space-y-2">
                <MessageSquare className="w-8 h-8 text-slate-300 mx-auto" />
                <p className="text-sm font-semibold text-slate-700">No conversations yet</p>
                <p className="text-xs text-slate-400">
                  Select a variable repair service or provider profile to start chatting.
                </p>
              </div>
            ) : (
              conversations.map((conv) => {
                const isSelected = conv.id === activeConvId
                return (
                  <button
                    key={conv.id}
                    onClick={() => setActiveConvId(conv.id)}
                    className={`w-full text-left p-4 transition-colors flex items-start gap-3 hover:bg-slate-50 ${
                      isSelected ? 'bg-blue-50/60 border-l-4 border-blue-600' : ''
                    }`}
                  >
                    <div className="w-10 h-10 rounded-xl bg-blue-100 text-blue-700 font-bold flex items-center justify-center flex-shrink-0">
                      {conv.otherPartyName.charAt(0)}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between gap-1">
                        <p className="text-sm font-bold text-slate-900 truncate">
                          {conv.otherPartyName}
                        </p>
                        {conv.lastMessageAt && (
                          <span className="text-[10px] text-slate-400 flex-shrink-0">
                            {formatTime(conv.lastMessageAt)}
                          </span>
                        )}
                      </div>
                      {conv.serviceName && (
                        <span className="inline-block text-[10px] font-semibold text-blue-600 bg-blue-50 px-1.5 py-0.5 rounded mt-0.5 truncate max-w-full">
                          {conv.serviceName}
                        </span>
                      )}
                      <p className="text-xs text-slate-500 truncate mt-1">
                        {conv.lastMessage || 'No messages yet'}
                      </p>
                    </div>
                  </button>
                )
              })
            )}
          </div>
        </div>

        {/* Right Panel: Chat Area */}
        <div className={`md:col-span-8 flex flex-col ${!activeConvId ? 'hidden md:flex' : 'flex'}`}>
          {activeConvError ? (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center text-slate-500">
              <AlertCircle className="w-10 h-10 text-rose-500 mb-2" />
              <p className="text-sm font-bold text-slate-800">Unable to load messages. Please try again.</p>
              <button
                type="button"
                onClick={() => refetchActive()}
                className="mt-3 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-xs font-bold transition-all shadow-sm"
              >
                Retry
              </button>
            </div>
          ) : activeConv ? (
            <>
              {/* Chat Top Header */}
              <div className="p-4 border-b border-slate-100 flex items-center justify-between gap-3 bg-white">
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setActiveConvId(null)}
                    className="md:hidden p-1.5 rounded-lg text-slate-500 hover:bg-slate-100"
                    aria-label="Back to conversations"
                  >
                    <ArrowLeft className="w-5 h-5" />
                  </button>
                  <div className="w-9 h-9 rounded-xl bg-blue-600 text-white font-bold flex items-center justify-center shadow-sm">
                    {activeConv.otherPartyName.charAt(0)}
                  </div>
                  <div>
                    <h2 className="text-sm font-bold text-slate-900">
                      {activeConv.otherPartyName}
                    </h2>
                    {activeConv.serviceName && (
                      <p className="text-xs text-blue-600 font-medium">
                        Service: {activeConv.serviceName}
                      </p>
                    )}
                  </div>
                </div>

                <button
                  onClick={() => navigate(`/customer/providers/${activeConv.providerId}`)}
                  className="text-xs font-semibold text-slate-600 hover:text-blue-600 border border-slate-200 px-3 py-1.5 rounded-lg hover:bg-slate-50 transition-colors"
                >
                  View Profile
                </button>
              </div>

              {/* Chat Safety Notice Banner */}
              <div className="bg-amber-50/70 border-b border-amber-200/50 px-4 py-2 flex items-center gap-2 text-xs text-amber-800">
                <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0" />
                <span>
                  <strong>Safe Chat:</strong> Phone numbers, email addresses, and inappropriate language are automatically masked to safeguard customer privacy.
                </span>
              </div>

              {/* Messages & Quotes Stream */}
              <div className="flex-1 p-4 overflow-y-auto space-y-4 bg-slate-50/30">
                {/* Official Quotes Cards Section */}
                {activeConv.quotes && activeConv.quotes.length > 0 && (
                  <div className="space-y-3 mb-6">
                    {activeConv.quotes.map((quote) => (
                      <div
                        key={quote.id}
                        className={`rounded-2xl border p-4 shadow-sm transition-all ${
                          quote.status === 'ACCEPTED'
                            ? 'bg-emerald-50/60 border-emerald-300 ring-1 ring-emerald-400/20'
                            : quote.status === 'DECLINED'
                            ? 'bg-slate-50 border-slate-200 opacity-70'
                            : 'bg-amber-50/60 border-amber-300 ring-1 ring-amber-400/20'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className="text-xs font-bold uppercase tracking-wider text-slate-600">
                                Official Service Quote #{quote.id}
                              </span>
                              <span
                                className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                  quote.status === 'ACCEPTED'
                                    ? 'bg-emerald-600 text-white'
                                    : quote.status === 'DECLINED'
                                    ? 'bg-rose-100 text-rose-700'
                                    : quote.status === 'EXPIRED'
                                    ? 'bg-slate-200 text-slate-700'
                                    : 'bg-amber-500 text-white'
                                }`}
                              >
                                {quote.status}
                              </span>
                            </div>
                            <h3 className="text-base font-bold text-slate-900">
                              {quote.description}
                            </h3>
                            {quote.note && (
                              <p className="text-xs text-slate-600 italic">
                                Note: {quote.note}
                              </p>
                            )}
                          </div>

                          <div className="text-right">
                            <span className="text-xs text-slate-500 block">Agreed Price</span>
                            <span className="text-xl font-black text-slate-900">
                              {formatPrice(quote.amount)}
                            </span>
                          </div>
                        </div>

                        {/* Customer Actions on Quote */}
                        <div className="mt-4 pt-3 border-t border-slate-200/60 flex flex-wrap items-center justify-between gap-2">
                          <span className="text-[11px] text-slate-400">
                            Issued: {formatDate(quote.createdAt)}
                          </span>

                          <div className="flex items-center gap-2">
                            {quote.status === 'PENDING' && (
                              <>
                                <button
                                  onClick={() => declineQuoteMutation.mutate(quote.id)}
                                  disabled={declineQuoteMutation.isPending}
                                  className="px-3 py-1.5 rounded-lg border border-slate-300 hover:bg-slate-100 text-slate-700 text-xs font-semibold transition-colors"
                                >
                                  Decline
                                </button>
                                <button
                                  onClick={() => acceptQuoteMutation.mutate(quote.id)}
                                  disabled={acceptQuoteMutation.isPending}
                                  className="px-4 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-bold transition-colors shadow-sm flex items-center gap-1.5"
                                >
                                  <CheckCircle2 className="w-3.5 h-3.5" />
                                  Accept Quote
                                </button>
                              </>
                            )}

                            {quote.status === 'EXPIRED' && (
                              <span className="text-xs text-rose-600 font-semibold flex items-center gap-1">
                                <Clock className="w-3.5 h-3.5" />
                                Quote Expired
                              </span>
                            )}

                            {quote.status === 'ACCEPTED' && (
                              <button
                                onClick={() => setBookingModalQuote(quote)}
                                className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold transition-all shadow-sm flex items-center gap-1.5"
                              >
                                <Lock className="w-3.5 h-3.5" />
                                Book Now with Agreed Quote ({formatPrice(quote.amount)})
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Message bubbles */}
                {activeConv.messages && activeConv.messages.length > 0 ? (
                  activeConv.messages.map((msg) => {
                    const isCustomer = msg.senderRole === 'CUSTOMER'
                    return (
                      <div
                        key={msg.id}
                        className={`flex flex-col ${isCustomer ? 'items-end' : 'items-start'}`}
                      >
                        <div className="flex items-center gap-1 text-[11px] text-slate-400 mb-1 px-1">
                          <span>{isCustomer ? 'You' : activeConv.otherPartyName}</span>
                          <span>·</span>
                          <span>{formatTime(msg.createdAt)}</span>
                        </div>
                        <div
                          className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm shadow-sm ${
                            isCustomer
                              ? 'bg-blue-600 text-white rounded-tr-none'
                              : 'bg-white border border-slate-200/80 text-slate-800 rounded-tl-none'
                          }`}
                        >
                          <p className="whitespace-pre-wrap break-words leading-relaxed">
                            {msg.message}
                          </p>
                        </div>
                      </div>
                    )
                  })
                ) : (
                  <div className="text-center py-8 text-slate-400 text-xs">
                    No messages yet. Start the conversation.
                  </div>
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Chat Input Bar */}
              <form onSubmit={handleSendMessage} className="p-3 border-t border-slate-200/80 bg-white flex items-center gap-2">
                <input
                  type="text"
                  value={messageText}
                  onChange={(e) => setMessageText(e.target.value)}
                  placeholder="Type your message (e.g. device model, repair issue, preferred time)..."
                  className="flex-1 px-4 py-2.5 text-sm rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  disabled={sendMutation.isPending}
                />
                <button
                  type="submit"
                  disabled={!messageText.trim() || sendMutation.isPending}
                  className="px-4 py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white text-sm font-bold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
                >
                  <Send className="w-4 h-4" />
                  <span className="hidden sm:inline">Send</span>
                </button>
              </form>
            </>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center p-8 text-center text-slate-400">
              <MessageSquare className="w-12 h-12 text-slate-300 mb-2" />
              <p className="text-base font-bold text-slate-700">Select a Conversation</p>
              <p className="text-xs text-slate-500 max-w-sm mt-1">
                Choose a conversation from the left to view messages and quotes, or start an inquiry from a service card.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* BOOKING MODAL WITH AGREED QUOTE */}
      {bookingModalQuote && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full p-6 space-y-5 max-h-[90vh] overflow-y-auto">
            <div className="flex items-start justify-between border-b border-slate-100 pb-4">
              <div>
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  <Lock className="w-3 h-3" /> Quote-Locked Price
                </span>
                <h2 className="text-lg font-bold text-slate-900 mt-1">
                  Complete Booking with Agreed Quote
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  Quote #{bookingModalQuote.id} · {bookingModalQuote.description}
                </p>
              </div>
              <button
                onClick={() => setBookingModalQuote(null)}
                className="p-1 rounded-full text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                ✕
              </button>
            </div>

            {/* Price Snapshot Lock Banner */}
            <div className="bg-blue-50/60 border border-blue-200 rounded-2xl p-4 flex items-center justify-between">
              <div>
                <span className="text-xs text-blue-700 font-semibold block">Agreed Official Quote</span>
                <span className="text-xs text-slate-500">Locked directly from provider quote</span>
              </div>
              <span className="text-2xl font-black text-slate-900">
                {formatPrice(bookingModalQuote.amount)}
              </span>
            </div>

            {/* Booking Form Fields */}
            <div className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Preferred Date *
                </label>
                <input
                  type="date"
                  value={bookingDate}
                  min={new Date().toISOString().split('T')[0]}
                  onChange={(e) => setBookingDate(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                  required
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Preferred Time *
                </label>
                <input
                  type="time"
                  value={bookingTime}
                  onChange={(e) => setBookingTime(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                  required
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Service Address *
                </label>
                <textarea
                  value={serviceAddress}
                  onChange={(e) => setServiceAddress(e.target.value)}
                  placeholder="Enter full address where service will be performed..."
                  rows={2}
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                  required
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Additional Notes (Optional)
                </label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="Gate code, specific instructions, etc."
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-sm"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setBookingModalQuote(null)}
                className="px-4 py-2 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => bookAgreedMutation.mutate()}
                disabled={bookAgreedMutation.isPending || !bookingDate || !serviceAddress.trim()}
                className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 disabled:opacity-50 text-white font-bold transition-all shadow-sm flex items-center gap-1.5"
              >
                {bookAgreedMutation.isPending ? 'Confirming...' : `Confirm Booking (${formatPrice(bookingModalQuote.amount)})`}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
