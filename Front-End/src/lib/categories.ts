import React from 'react'
import {
  Layers,
  Tv,
  Smartphone,
  Laptop,
  Car,
  Wrench,
  Zap,
  Home,
} from 'lucide-react'

export interface CategoryMeta {
  name: string
  label: string
  icon: React.ElementType
  description: string
  examples: string[]
}

export const MAJOR_CATEGORIES: CategoryMeta[] = [
  {
    name: '',
    label: 'All Services',
    icon: Layers,
    description: 'Browse all available maintenance and repair services',
    examples: ['AC repair', 'Mobile repair', 'Plumbing', 'Electrical'],
  },
  {
    name: 'Home Appliances',
    label: 'Home Appliances',
    icon: Tv,
    description: 'AC, Refrigerator, TV, Washing Machine, RO purifier repair',
    examples: ['AC Repair', 'TV Repair', 'Refrigerator Repair', 'Washing Machine Repair', 'RO Service'],
  },
  {
    name: 'Mobile & Tablet',
    label: 'Mobile & Tablet',
    icon: Smartphone,
    description: 'Screen replacement, battery swap, charging port fixing',
    examples: ['Mobile Repair', 'Screen Replacement', 'Tablet Screen & Battery'],
  },
  {
    name: 'Laptop & Computer',
    label: 'Laptop & Computer',
    icon: Laptop,
    description: 'Laptop repair, OS installation, hardware troubleshooting, PC assembly',
    examples: ['Laptop Repair', 'OS Diagnosis', 'Desktop Computer Repair'],
  },
  {
    name: 'Automobile',
    label: 'Automobile',
    icon: Car,
    description: 'Car & bike general service, oil change, brake check, doorstep tune-up',
    examples: ['Car & Bike General Service', 'Oil Change', 'Brake Tune-up'],
  },
  {
    name: 'Plumbing',
    label: 'Plumbing',
    icon: Wrench,
    description: 'Pipe leakage, drainage cleaning, tap & sanitary fixture installation',
    examples: ['Plumbing Leakage', 'Pipe Repair', 'Tap & Fixture Installation'],
  },
  {
    name: 'Electrical',
    label: 'Electrical',
    icon: Zap,
    description: 'Wiring inspection, short circuit fix, switchboard & breaker repair',
    examples: ['Electrical Inspection', 'Emergency Electrical Repair', 'Wiring Fix'],
  },
  {
    name: 'Home Services',
    label: 'Home Services',
    icon: Home,
    description: 'Full house deep cleaning, sanitization, painting, home repair',
    examples: ['Deep Home Cleaning', 'Home Repair', 'Painting'],
  },
]

export const POPULAR_SERVICE_SEARCHES = [
  'AC Repair',
  'Mobile Repair',
  'Refrigerator Repair',
  'Washing Machine Repair',
  'TV Repair',
  'Laptop Repair',
  'Plumbing',
  'Electrical Repair',
  'Car & Bike Service',
  'RO Water Purifier',
]
