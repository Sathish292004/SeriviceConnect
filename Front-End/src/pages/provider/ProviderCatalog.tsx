import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, Trash2, Edit2, Clock, Briefcase, Sparkles } from 'lucide-react'
import { catalogApi } from '@/api/catalog'
import { providerApi } from '@/api/provider'
import { Modal } from '@/components/shared/Modal'
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'
import { formatPrice } from '@/utils/formatters'
import type { CatalogItem } from '@/types'

export default function ProviderCatalog() {
  const qc = useQueryClient()
  const [modalOpen, setModalOpen] = useState(false)
  const [editingItem, setEditingItem] = useState<CatalogItem | null>(null)
  const [deleteId, setDeleteId] = useState<number | null>(null)

  // Form fields
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const [category, setCategory] = useState('General')
  const [price, setPrice] = useState('')
  const [duration, setDuration] = useState('60')

  const { data: provider } = useQuery({
    queryKey: ['provider', 'me'],
    queryFn: () => providerApi.getMe(),
    select: (r) => r.data,
  })

  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['catalog', 'provider', provider?.id],
    queryFn: () => catalogApi.getByProvider(provider!.id, { size: 50 }),
    select: (r) => r.data,
    enabled: !!provider?.id,
  })

  const createMutation = useMutation({
    mutationFn: () =>
      catalogApi.create({
        name,
        description: description.trim() || undefined,
        category,
        price: Number(price),
        durationMinutes: duration ? Number(duration) : undefined,
      }),
    onSuccess: () => {
      toast.success('Service added to your public catalog!')
      closeForm()
      qc.invalidateQueries({ queryKey: ['catalog'] })
    },
    onError: () => toast.error('Failed to add service.'),
  })

  const updateMutation = useMutation({
    mutationFn: () =>
      catalogApi.update(editingItem!.id, {
        name,
        description: description.trim() || undefined,
        category,
        price: Number(price),
        durationMinutes: duration ? Number(duration) : undefined,
      }),
    onSuccess: () => {
      toast.success('Service updated successfully!')
      closeForm()
      qc.invalidateQueries({ queryKey: ['catalog'] })
    },
    onError: () => toast.error('Failed to update service.'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => catalogApi.deactivate(id),
    onSuccess: () => {
      toast.success('Service deactivated.')
      setDeleteId(null)
      qc.invalidateQueries({ queryKey: ['catalog'] })
    },
    onError: () => toast.error('Failed to deactivate service.'),
  })

  function openCreate() {
    setEditingItem(null)
    setName('')
    setDescription('')
    setCategory('General')
    setPrice('')
    setDuration('60')
    setModalOpen(true)
  }

  function openEdit(item: CatalogItem) {
    setEditingItem(item)
    setName(item.name)
    setDescription(item.description ?? '')
    setCategory(item.category)
    setPrice(String(item.price))
    setDuration(item.durationMinutes ? String(item.durationMinutes) : '60')
    setModalOpen(true)
  }

  function closeForm() {
    setModalOpen(false)
    setEditingItem(null)
  }

  const items = (data?.content ?? []).filter((i) => i.active)

  return (
    <div className="space-y-6 max-w-5xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Service Catalog & Offerings
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-1">
            Define the services, rates, and durations available for customer booking.
          </p>
        </div>
        <button
          onClick={openCreate}
          className="inline-flex items-center gap-1.5 px-5 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm transition-all self-start sm:self-auto"
        >
          <Plus className="w-4 h-4" />
          <span>Add New Service</span>
        </button>
      </div>

      {isLoading ? (
        <LoadingState message="Loading your services..." />
      ) : error ? (
        <ErrorState
          message="Failed to load service catalog."
          action={{ label: 'Retry', onClick: () => refetch() }}
        />
      ) : items.length === 0 ? (
        <EmptyState
          title="No active services in catalog"
          message="Add your first service to start receiving appointments from local customers."
          action={{ label: 'Create First Service', onClick: openCreate }}
        />
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {items.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded-2xl p-6 border border-slate-200/80 hover:shadow-md transition-all flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between gap-2 mb-2">
                  <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700">
                    {item.category}
                  </span>
                  {item.durationMinutes && (
                    <span className="text-xs text-slate-400 flex items-center gap-1 font-medium">
                      <Clock className="w-3.5 h-3.5" />
                      ~{item.durationMinutes} min
                    </span>
                  )}
                </div>

                <h3 className="text-base font-bold text-slate-900">{item.name}</h3>
                {item.description && (
                  <p className="text-xs text-slate-500 mt-1.5 leading-relaxed line-clamp-2">
                    {item.description}
                  </p>
                )}
              </div>

              <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between">
                <div>
                  <span className="text-[11px] text-slate-400 block">Rate</span>
                  <span className="text-lg font-black text-slate-900">
                    {formatPrice(item.price)}
                  </span>
                </div>

                <div className="flex items-center gap-1.5">
                  <button
                    onClick={() => openEdit(item)}
                    className="p-2 rounded-xl text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    title="Edit Service"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setDeleteId(item.id)}
                    className="p-2 rounded-xl text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                    title="Deactivate Service"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Add / Edit Modal */}
      <Modal
        isOpen={modalOpen}
        onClose={closeForm}
        title={editingItem ? 'Edit Service Offering' : 'Add New Service Offering'}
        description="Set transparent pricing and details for your clients"
        maxWidth="lg"
      >
        <div className="space-y-4 pt-2">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Service Name <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="e.g. Master Bathroom Leak Repair"
                className="w-full mt-1 p-3 rounded-xl border border-slate-200 text-sm text-slate-800"
              />
            </div>

            <div>
              <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full mt-1 p-3 rounded-xl border border-slate-200 text-sm text-slate-800 bg-white"
              >
                <option value="General">General</option>
                <option value="Electrical">Electrical</option>
                <option value="Plumbing">Plumbing</option>
                <option value="HVAC">HVAC & AC</option>
                <option value="Cleaning">Cleaning</option>
                <option value="Painting">Painting</option>
                <option value="Appliance">Appliance Repair</option>
              </select>
            </div>
          </div>

          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Description (Optional)
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Detail what is included in this service..."
              className="w-full mt-1 p-3 rounded-xl border border-slate-200 text-sm text-slate-800 min-h-[80px]"
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Price (₹) <span className="text-rose-500">*</span>
              </label>
              <input
                type="number"
                value={price}
                onChange={(e) => setPrice(e.target.value)}
                placeholder="e.g. 799"
                className="w-full mt-1 p-3 rounded-xl border border-slate-200 text-sm text-slate-800 font-bold"
              />
            </div>

            <div>
              <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
                Estimated Duration (Minutes)
              </label>
              <input
                type="number"
                value={duration}
                onChange={(e) => setDuration(e.target.value)}
                placeholder="e.g. 60"
                className="w-full mt-1 p-3 rounded-xl border border-slate-200 text-sm text-slate-800"
              />
            </div>
          </div>

          <div className="pt-4 flex justify-end gap-2.5">
            <button
              type="button"
              onClick={closeForm}
              className="px-4 py-2.5 rounded-xl border border-slate-200 text-slate-700 text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={() => {
                if (!name.trim()) {
                  toast.error('Service name is required')
                  return
                }
                if (!price || Number(price) <= 0) {
                  toast.error('Please enter a valid price')
                  return
                }
                if (editingItem) updateMutation.mutate()
                else createMutation.mutate()
              }}
              disabled={createMutation.isPending || updateMutation.isPending}
              className="px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm"
            >
              {editingItem ? 'Save Changes' : 'Create Service'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Delete confirmation dialog */}
      <ConfirmationDialog
        isOpen={!!deleteId}
        onClose={() => setDeleteId(null)}
        onConfirm={() => deleteId && deleteMutation.mutate(deleteId)}
        isLoading={deleteMutation.isPending}
        title="Deactivate Service?"
        message="Are you sure you want to deactivate this service? It will no longer appear in your public profile for customer bookings."
        confirmLabel="Yes, Deactivate"
        cancelLabel="Keep Service"
        variant="danger"
      />
    </div>
  )
}
