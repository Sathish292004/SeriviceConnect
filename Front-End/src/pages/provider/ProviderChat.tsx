import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Send, ShieldAlert, CheckCircle2, XCircle, Plus,
  MessageSquare, RefreshCw, FileText, ArrowLeft, Clock, AlertCircle
} from 'lucide-react'
import { chatApi } from '@/api/chat'
import { catalogApi } from '@/api/catalog'
import { providerApi } from '@/api/provider'
import { formatPrice, formatDate, formatTime } from '@/utils/formatters'
import type { Conversation, Message, Quote, CatalogItem } from '@/types'

export default function ProviderChat() {
  const [searchParams] = useSearchParams()
  const qc = useQueryClient()

  const [activeConvId, setActiveConvId] = useState<number | null>(null)
  const [messageText, setMessageText] = useState('')
  const [quoteModalOpen, setQuoteModalOpen] = useState(false)

  // Quote form state
  const [quoteDescription, setQuoteDescription] = useState('')
  const [quoteAmount, setQuoteAmount] = useState('')
  const [quoteNote, setQuoteNote] = useState('')
  const [quoteServiceId, setQuoteServiceId] = useState<number | undefined>(undefined)

  const messagesEndRef = useRef<HTMLDivElement>(null)

  // Fetch current provider info
  const { data: provider } = useQuery({
    queryKey: ['provider', 'me'],
    queryFn: () => providerApi.getMe(),
    select: (r) => r.data,
  })

  // Fetch provider's catalog items for quote preset dropdown
  const { data: catalog } = useQuery({
    queryKey: ['catalog', 'provider', provider?.id],
    queryFn: () => catalogApi.getByProvider(provider!.id, { size: 50 }),
    select: (r) => r.data?.content ?? [],
    enabled: !!provider?.id,
  })

  // Fetch all conversations
  const { data: conversations, isLoading: loadingConvs, refetch: refetchConvs } = useQuery({
    queryKey: ['chat', 'provider-conversations'],
    queryFn: () => chatApi.getConversations(),
    select: (r) => r.data,
    refetchInterval: 4000,
  })

  // Select first conversation if none selected
  useEffect(() => {
    if (!activeConvId && conversations && conversations.length > 0) {
      setActiveConvId(conversations[0].id)
    }
  }, [conversations, activeConvId])

  // Fetch active conversation details
  const { data: activeConv, isLoading: loadingActive, isError: activeConvError, refetch: refetchActive } = useQuery({
    queryKey: ['chat', 'provider-conversation', activeConvId],
    queryFn: () => chatApi.getConversation(activeConvId!),
    select: (r) => r.data,
    enabled: !!activeConvId,
    refetchInterval: 3000,
  })

  // Scroll to bottom on new messages
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
      qc.invalidateQueries({ queryKey: ['chat', 'provider-conversation', activeConvId] })
      qc.invalidateQueries({ queryKey: ['chat', 'provider-conversations'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Failed to send message')
    },
  })

  // Create quote mutation
  const createQuoteMutation = useMutation({
    mutationFn: () => {
      if (!activeConvId) throw new Error('No active conversation')
      if (!quoteDescription.trim()) throw new Error('Quote description is required')
      const amt = Number(quoteAmount)
      if (isNaN(amt) || amt <= 0) throw new Error('Valid quote amount is required')

      return chatApi.createQuote(activeConvId, {
        serviceId: quoteServiceId,
        description: quoteDescription.trim(),
        amount: amt,
        note: quoteNote.trim() || undefined,
      })
    },
    onSuccess: () => {
      toast.success('Official quote sent to customer!')
      setQuoteModalOpen(false)
      setQuoteDescription('')
      setQuoteAmount('')
      setQuoteNote('')
      setQuoteServiceId(undefined)
      qc.invalidateQueries({ queryKey: ['chat', 'provider-conversation', activeConvId] })
      qc.invalidateQueries({ queryKey: ['chat', 'provider-conversations'] })
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || err?.message || 'Failed to create quote')
    },
  })

  const handleSendMessage = (e: React.FormEvent) => {
    e.preventDefault()
    if (!messageText.trim() || sendMutation.isPending) return
    sendMutation.mutate(messageText.trim())
  }

  const handlePresetSelect = (serviceIdStr: string) => {
    const sId = Number(serviceIdStr)
    if (!sId) {
      setQuoteServiceId(undefined)
      return
    }
    const found = catalog?.find((c) => c.id === sId)
    if (found) {
      setQuoteServiceId(found.id)
      setQuoteDescription(found.name)
      setQuoteAmount(String(found.price))
    }
  }

  return (
    <div className="max-w-6xl mx-auto space-y-4">
      {/* Top Banner */}
      <div className="bg-white rounded-2xl p-4 sm:p-5 border border-slate-200/80 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-xl sm:text-2xl font-black text-slate-900 flex items-center gap-2">
            <MessageSquare className="w-6 h-6 text-indigo-600" />
            Customer Messages & Custom Quotes
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Respond to customer repair inquiries, understand requirements, and issue official binding price quotes.
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
              Customer Inquiries ({conversations?.length ?? 0})
            </h2>
          </div>

          <div className="flex-1 overflow-y-auto divide-y divide-slate-100">
            {loadingConvs ? (
              <div className="p-6 text-center text-sm text-slate-400">Loading messages...</div>
            ) : !conversations || conversations.length === 0 ? (
              <div className="p-6 text-center space-y-2">
                <MessageSquare className="w-8 h-8 text-slate-300 mx-auto" />
                <p className="text-sm font-semibold text-slate-700">No inquiries yet</p>
                <p className="text-xs text-slate-400">
                  When customers send inquiries about your repair offerings, they will appear here.
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
                      isSelected ? 'bg-indigo-50/60 border-l-4 border-indigo-600' : ''
                    }`}
                  >
                    <div className="w-10 h-10 rounded-xl bg-indigo-100 text-indigo-700 font-bold flex items-center justify-center flex-shrink-0">
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
                        <span className="inline-block text-[10px] font-semibold text-indigo-600 bg-indigo-50 px-1.5 py-0.5 rounded mt-0.5 truncate max-w-full">
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
                className="mt-3 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold transition-all shadow-sm"
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
                  <div className="w-9 h-9 rounded-xl bg-indigo-600 text-white font-bold flex items-center justify-center shadow-sm">
                    {activeConv.otherPartyName.charAt(0)}
                  </div>
                  <div>
                    <h2 className="text-sm font-bold text-slate-900">
                      {activeConv.otherPartyName}
                    </h2>
                    {activeConv.serviceName && (
                      <p className="text-xs text-indigo-600 font-medium">
                        Referenced Service: {activeConv.serviceName}
                      </p>
                    )}
                  </div>
                </div>

                {/* Create Quote Button */}
                <button
                  onClick={() => setQuoteModalOpen(true)}
                  className="px-4 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold transition-all shadow-sm flex items-center gap-1.5"
                >
                  <Plus className="w-4 h-4" />
                  <span>Create Quote</span>
                </button>
              </div>

              {/* Chat Safety Notice Banner */}
              <div className="bg-amber-50/70 border-b border-amber-200/50 px-4 py-2 flex items-center gap-2 text-xs text-amber-800">
                <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0" />
                <span>
                  <strong>Safety Protocol:</strong> Phone numbers, email addresses, and prohibited terms are masked by the server to maintain marketplace safety.
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
                            : 'bg-indigo-50/60 border-indigo-300 ring-1 ring-indigo-400/20'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div className="space-y-1">
                            <div className="flex items-center gap-2">
                              <span className="text-xs font-bold uppercase tracking-wider text-slate-600">
                                Your Official Quote #{quote.id}
                              </span>
                              <span
                                className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                                  quote.status === 'ACCEPTED'
                                    ? 'bg-emerald-600 text-white'
                                    : quote.status === 'DECLINED'
                                    ? 'bg-rose-100 text-rose-700'
                                    : quote.status === 'EXPIRED'
                                    ? 'bg-slate-200 text-slate-700'
                                    : 'bg-indigo-600 text-white'
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
                                Note to customer: {quote.note}
                              </p>
                            )}
                          </div>

                          <div className="text-right">
                            <span className="text-xs text-slate-500 block">Offered Amount</span>
                            <span className="text-xl font-black text-slate-900">
                              {formatPrice(quote.amount)}
                            </span>
                          </div>
                        </div>

                        <div className="mt-3 pt-2 border-t border-slate-200/60 flex items-center justify-between text-[11px] text-slate-500">
                          <span>Issued: {formatDate(quote.createdAt)}</span>
                          <span>
                            {quote.status === 'ACCEPTED' && 'Customer accepted quote. Awaiting booking completion.'}
                            {quote.status === 'PENDING' && 'Pending customer acceptance.'}
                            {quote.status === 'DECLINED' && 'Customer declined this quote.'}
                            {quote.status === 'EXPIRED' && 'Quote expired.'}
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}

                {/* Message Bubbles */}
                {activeConv.messages && activeConv.messages.length > 0 ? (
                  activeConv.messages.map((msg) => {
                    const isProvider = msg.senderRole === 'PROVIDER'
                    return (
                      <div
                        key={msg.id}
                        className={`flex flex-col ${isProvider ? 'items-end' : 'items-start'}`}
                      >
                        <div className="flex items-center gap-1 text-[11px] text-slate-400 mb-1 px-1">
                          <span>{isProvider ? 'You' : activeConv.otherPartyName}</span>
                          <span>·</span>
                          <span>{formatTime(msg.createdAt)}</span>
                        </div>
                        <div
                          className={`max-w-[80%] rounded-2xl px-4 py-2.5 text-sm shadow-sm ${
                            isProvider
                              ? 'bg-indigo-600 text-white rounded-tr-none'
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
                  placeholder="Type your response to the customer..."
                  className="flex-1 px-4 py-2.5 text-sm rounded-xl border border-slate-300 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                  disabled={sendMutation.isPending}
                />
                <button
                  type="submit"
                  disabled={!messageText.trim() || sendMutation.isPending}
                  className="px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white text-sm font-bold rounded-xl transition-all shadow-sm flex items-center gap-1.5"
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
                Choose a customer conversation from the left to read inquiries, discuss diagnostics, and create official quotes.
              </p>
            </div>
          )}
        </div>
      </div>

      {/* CREATE QUOTE MODAL */}
      {quoteModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="bg-white rounded-3xl shadow-2xl max-w-lg w-full p-6 space-y-5 max-h-[90vh] overflow-y-auto">
            <div className="flex items-start justify-between border-b border-slate-100 pb-4">
              <div>
                <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-50 text-indigo-700 border border-indigo-200">
                  <FileText className="w-3 h-3" /> Official Binding Quote
                </span>
                <h2 className="text-lg font-bold text-slate-900 mt-1">
                  Create Quote for {activeConv?.otherPartyName}
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  The customer will review and accept this quote before completing their booking.
                </p>
              </div>
              <button
                onClick={() => setQuoteModalOpen(false)}
                className="p-1 rounded-full text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                ✕
              </button>
            </div>

            {/* Catalog Preset Selector */}
            {catalog && catalog.length > 0 && (
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">
                  Fill from Catalog Offering (Optional)
                </label>
                <select
                  value={quoteServiceId ?? ''}
                  onChange={(e) => handlePresetSelect(e.target.value)}
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none text-xs bg-slate-50"
                >
                  <option value="">-- Custom Diagnostic / Service --</option>
                  {catalog.map((cat) => (
                    <option key={cat.id} value={cat.id}>
                      {cat.name} ({formatPrice(cat.price)})
                    </option>
                  ))}
                </select>
              </div>
            )}

            {/* Quote Form */}
            <div className="space-y-4 text-xs">
              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Service Description & Scope of Work *
                </label>
                <textarea
                  value={quoteDescription}
                  onChange={(e) => setQuoteDescription(e.target.value)}
                  placeholder="e.g. iPhone 13 Genuine Battery Replacement with 6 months warranty"
                  rows={2}
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none text-sm"
                  required
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Total Agreed Price (₹) *
                </label>
                <input
                  type="number"
                  value={quoteAmount}
                  onChange={(e) => setQuoteAmount(e.target.value)}
                  placeholder="3500"
                  min="1"
                  step="any"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none text-sm font-bold text-slate-900"
                  required
                />
              </div>

              <div>
                <label className="block font-bold text-slate-700 mb-1">
                  Notes / Instructions for Customer (Optional)
                </label>
                <input
                  type="text"
                  value={quoteNote}
                  onChange={(e) => setQuoteNote(e.target.value)}
                  placeholder="e.g. Please bring device before 4 PM with 30% battery charge"
                  className="w-full px-3 py-2 border rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none text-sm"
                />
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setQuoteModalOpen(false)}
                className="px-4 py-2 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:bg-slate-50 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={() => createQuoteMutation.mutate()}
                disabled={createQuoteMutation.isPending || !quoteDescription.trim() || !quoteAmount}
                className="px-5 py-2 rounded-xl bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50 text-white font-bold transition-all shadow-sm flex items-center gap-1.5"
              >
                {createQuoteMutation.isPending ? 'Sending Quote...' : 'Send Official Quote'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
