import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Search, MapPin, CalendarCheck, CheckCircle2, Shield, Star } from 'lucide-react'

export default function Landing() {
  const navigate = useNavigate()
  const [heroSearch, setHeroSearch] = useState('')
  return (
    <div>
      {/* Hero */}
      <section className="bg-white border-b border-[#E2E8F0]">
        <div className="page-container py-16 md:py-24 text-center">
          <h1 className="text-4xl md:text-5xl font-bold text-[#0F172A] leading-tight">
            Find Trusted Local<br />Service Providers
          </h1>
          <p className="mt-4 text-lg text-[#64748B] max-w-2xl mx-auto">
            Book verified electricians, plumbers, HVAC technicians, and more — all near you.
          </p>
          {/* Hero service search */}
          <form
            onSubmit={(e) => {
              e.preventDefault()
              if (heroSearch.trim()) {
                navigate(`/providers?q=${encodeURIComponent(heroSearch.trim())}`)
              } else {
                navigate('/providers')
              }
            }}
            className="mt-8 max-w-xl mx-auto"
          >
            <div className="flex items-center bg-white rounded-xl border-2 border-[#E2E8F0] focus-within:border-[#2563EB] focus-within:ring-2 focus-within:ring-[#2563EB]/10 shadow-lg transition-all overflow-hidden">
              <Search className="w-5 h-5 text-[#94A3B8] ml-4 flex-shrink-0" />
              <input
                value={heroSearch}
                onChange={(e) => setHeroSearch(e.target.value)}
                placeholder="What service do you need? e.g. AC repair, plumbing…"
                className="flex-1 px-3 py-3.5 text-sm text-[#0F172A] placeholder-[#94A3B8] bg-transparent border-none outline-none"
              />
              <button
                type="submit"
                className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-medium text-sm px-5 py-3.5 transition-colors flex-shrink-0"
              >
                Search
              </button>
            </div>
          </form>
          <div className="mt-4 flex flex-col sm:flex-row items-center justify-center gap-3">
            <Link to="/providers" className="sc-btn-primary text-base px-8 py-3 inline-flex items-center gap-2">
              <MapPin className="w-5 h-5" /> Find Providers Near Me
            </Link>
            <Link to="/services" className="sc-btn-outline text-base px-8 py-3 inline-flex items-center gap-2">
              <Search className="w-5 h-5" /> Browse Services
            </Link>
          </div>
        </div>
      </section>

      {/* How it works */}
      <section className="page-container py-16">
        <h2 className="text-2xl font-bold text-[#0F172A] text-center mb-10">How It Works</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            { icon: Search, title: 'Search', desc: 'Find service providers near you by category, availability, and ratings.' },
            { icon: CalendarCheck, title: 'Book', desc: 'Choose your provider, pick a time, and book instantly.' },
            { icon: CheckCircle2, title: 'Done', desc: 'Get your service completed by a verified professional.' },
          ].map((step, i) => (
            <div key={i} className="sc-card p-6 text-center">
              <div className="w-14 h-14 rounded-full bg-[#EFF6FF] flex items-center justify-center mx-auto mb-4">
                <step.icon className="w-7 h-7 text-[#2563EB]" />
              </div>
              <h3 className="text-base font-semibold text-[#0F172A] mb-2">{step.title}</h3>
              <p className="text-sm text-[#64748B]">{step.desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Why choose us */}
      <section className="bg-white border-y border-[#E2E8F0]">
        <div className="page-container py-16">
          <h2 className="text-2xl font-bold text-[#0F172A] text-center mb-10">Why ServiceConnect</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {[
              { icon: Shield, title: 'Verified Providers', desc: 'Every provider is reviewed and approved.' },
              { icon: Star, title: 'Trusted Reviews', desc: 'Real reviews from real customers.' },
              { icon: MapPin, title: 'Live Map', desc: 'See providers near you in real-time.' },
              { icon: CalendarCheck, title: 'Instant Booking', desc: 'Book with a single click.' },
            ].map((item, i) => (
              <div key={i} className="text-center">
                <div className="w-12 h-12 rounded-[8px] bg-[#DCFCE7] flex items-center justify-center mx-auto mb-3">
                  <item.icon className="w-6 h-6 text-[#16A34A]" />
                </div>
                <h3 className="text-sm font-semibold text-[#0F172A] mb-1">{item.title}</h3>
                <p className="text-xs text-[#64748B]">{item.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="page-container py-16 text-center">
        <div className="sc-card p-10 bg-gradient-to-r from-[#2563EB] to-[#1D4ED8]">
          <h2 className="text-2xl font-bold text-white mb-3">Ready to get started?</h2>
          <p className="text-white/80 mb-6">Join thousands of customers finding trusted service providers.</p>
          <div className="flex flex-col sm:flex-row items-center justify-center gap-3">
            <Link to="/customer/register" className="bg-white text-[#2563EB] font-medium px-6 py-2.5 rounded-pill hover:bg-[#F1F5F9] transition-colors text-sm">
              Create Account
            </Link>
            <Link to="/provider/register" className="border border-white/30 text-white font-medium px-6 py-2.5 rounded-pill hover:bg-white/10 transition-colors text-sm">
              Join as Provider
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}
