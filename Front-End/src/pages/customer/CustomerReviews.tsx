import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useSearchParams, Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Edit2, Trash2, Star, CheckCircle2, MessageSquare, ArrowLeft } from 'lucide-react'
import { reviewApi } from '@/api/review'
import { bookingApi } from '@/api/booking'
import { useAuthStore } from '@/store/authStore'
import { StarRating } from '@/components/shared/StarRating'
import { Pagination } from '@/components/shared/Pagination'
import { Modal } from '@/components/shared/Modal'
import { ConfirmationDialog } from '@/components/shared/ConfirmationDialog'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'
import { formatDate } from '@/utils/formatters'
import type { Review } from '@/types'

export default function CustomerReviews() {
  const user = useAuthStore((s) => s.user)
  const qc = useQueryClient()
  const [searchParams] = useSearchParams()
  const bookingIdParam = searchParams.get('bookingId')

  const [page, setPage] = useState(0)
  const [editingReview, setEditingReview] = useState<Review | null>(null)
  const [editRating, setEditRating] = useState(5)
  const [editComment, setEditComment] = useState('')

  const [deleteTargetId, setDeleteTargetId] = useState<number | null>(null)

  const [newRating, setNewRating] = useState(5)
  const [newComment, setNewComment] = useState('')

  // Fetch booking if query param present
  const { data: bookingData } = useQuery({
    queryKey: ['booking', 'for-review', bookingIdParam],
    queryFn: () => bookingApi.getById(Number(bookingIdParam)),
    select: (r) => r.data,
    enabled: !!bookingIdParam,
  })

  // Fetch customer's past reviews
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['reviews', 'customer', user?.id, page],
    queryFn: () => reviewApi.getByCustomer(user!.id, { page, size: 10 }),
    select: (r) => r.data,
    enabled: !!user?.id,
  })

  const createMutation = useMutation({
    mutationFn: () =>
      reviewApi.create({
        bookingId: Number(bookingIdParam),
        providerId: bookingData?.providerId ?? 0,
        rating: newRating,
        comment: newComment.trim() || undefined,
      }),
    onSuccess: () => {
      toast.success('Review published successfully!')
      setNewComment('')
      setNewRating(5)
      qc.invalidateQueries({ queryKey: ['reviews'] })
    },
    onError: () =>
      toast.error('Failed to submit review. You may have already reviewed this appointment.'),
  })

  const updateMutation = useMutation({
    mutationFn: (id: number) =>
      reviewApi.update(id, {
        rating: editRating,
        comment: editComment.trim() || undefined,
      }),
    onSuccess: () => {
      toast.success('Review updated successfully!')
      setEditingReview(null)
      qc.invalidateQueries({ queryKey: ['reviews'] })
    },
    onError: () => toast.error('Failed to update review.'),
  })

  const deleteMutation = useMutation({
    mutationFn: (id: number) => reviewApi.delete(id),
    onSuccess: () => {
      toast.success('Review deleted.')
      setDeleteTargetId(null)
      qc.invalidateQueries({ queryKey: ['reviews'] })
    },
    onError: () => toast.error('Failed to delete review.'),
  })

  const reviews = data?.content ?? []

  const openEdit = (rev: Review) => {
    setEditingReview(rev)
    setEditRating(rev.rating)
    setEditComment(rev.comment ?? '')
  }

  return (
    <div className="max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
          My Ratings & Reviews
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 mt-1">
          Share your feedback on completed services to help the community.
        </p>
      </div>

      {/* Write a Review Card if bookingId parameter is provided */}
      {bookingIdParam && (
        <div className="bg-white rounded-3xl p-6 sm:p-8 border border-slate-200/80 shadow-sm space-y-4">
          <div className="flex items-center gap-2 text-blue-600">
            <MessageSquare className="w-5 h-5" />
            <h2 className="text-base font-bold text-slate-900">
              Leave a Review for Booking #{bookingIdParam}
            </h2>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Your Rating
            </label>
            <div className="pt-1">
              <StarRating value={newRating} onChange={setNewRating} size="lg" />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Detailed Feedback (Optional)
            </label>
            <textarea
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              placeholder="How was the pro's quality of work, punctuality, and professionalism?"
              className="w-full p-3.5 rounded-xl border border-slate-200 text-sm text-slate-800 placeholder-slate-400 min-h-[90px] focus:outline-none focus:ring-2 focus:ring-blue-500/20 focus:border-blue-600"
            />
          </div>

          <button
            onClick={() => createMutation.mutate()}
            disabled={createMutation.isPending || !bookingData}
            className="px-6 py-2.5 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-bold text-sm shadow-sm transition-all disabled:opacity-50"
          >
            {createMutation.isPending ? 'Submitting...' : 'Publish Review'}
          </button>
        </div>
      )}

      {/* Existing Reviews List */}
      <div className="space-y-3">
        <h2 className="text-base font-bold text-slate-900">Submitted Reviews</h2>

        {isLoading ? (
          <LoadingState message="Loading your past reviews..." />
        ) : error ? (
          <ErrorState
            message="Failed to retrieve reviews."
            action={{ label: 'Retry', onClick: () => refetch() }}
          />
        ) : reviews.length === 0 ? (
          <EmptyState
            title="No reviews submitted yet"
            message="Complete your appointments to rate and review service professionals."
          />
        ) : (
          <div className="space-y-3.5">
            {reviews.map((r) => (
              <div
                key={r.id}
                className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-sm flex flex-col justify-between gap-3"
              >
                <div>
                  <div className="flex items-center justify-between gap-2 mb-2">
                    <div className="flex items-center gap-2">
                      <StarRating value={r.rating} size="sm" />
                      <span className="text-xs font-bold text-slate-800">{r.rating} / 5</span>
                    </div>
                    <span className="text-xs text-slate-400 font-medium">
                      {formatDate(r.createdAt)}
                    </span>
                  </div>

                  <p className="text-xs font-medium text-slate-500 mb-1">
                    Booking #{r.bookingId} · Provider #{r.providerId}
                  </p>

                  {r.comment && (
                    <p className="text-sm text-slate-700 leading-relaxed bg-slate-50/75 p-3 rounded-xl border border-slate-100 mt-2">
                      "{r.comment}"
                    </p>
                  )}
                </div>

                <div className="flex items-center justify-end gap-2 pt-2 border-t border-slate-100">
                  <button
                    onClick={() => openEdit(r)}
                    className="p-2 rounded-lg text-slate-500 hover:text-blue-600 hover:bg-blue-50 transition-colors"
                    title="Edit Review"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => setDeleteTargetId(r.id)}
                    className="p-2 rounded-lg text-slate-500 hover:text-rose-600 hover:bg-rose-50 transition-colors"
                    title="Delete Review"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            ))}

            {data && data.totalPages > 1 && (
              <Pagination page={page} totalPages={data.totalPages} onPageChange={setPage} />
            )}
          </div>
        )}
      </div>

      {/* Edit Review Modal */}
      <Modal
        isOpen={!!editingReview}
        onClose={() => setEditingReview(null)}
        title="Edit Your Review"
        description="Update your rating score and feedback comments"
        maxWidth="md"
      >
        <div className="space-y-4 pt-2">
          <div className="space-y-1">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Rating
            </label>
            <div className="pt-1">
              <StarRating value={editRating} onChange={setEditRating} size="lg" />
            </div>
          </div>

          <div className="space-y-1">
            <label className="text-xs font-bold uppercase tracking-wider text-slate-500">
              Comment
            </label>
            <textarea
              value={editComment}
              onChange={(e) => setEditComment(e.target.value)}
              className="w-full p-3 rounded-xl border border-slate-200 text-sm text-slate-800 min-h-[90px]"
            />
          </div>

          <div className="flex justify-end gap-2.5 pt-3">
            <button
              onClick={() => setEditingReview(null)}
              className="px-4 py-2 rounded-xl border border-slate-200 text-slate-700 text-xs font-semibold"
            >
              Cancel
            </button>
            <button
              onClick={() => editingReview && updateMutation.mutate(editingReview.id)}
              disabled={updateMutation.isPending}
              className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-xs font-bold shadow-sm"
            >
              {updateMutation.isPending ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </div>
      </Modal>

      {/* Delete Confirmation Dialog */}
      <ConfirmationDialog
        isOpen={!!deleteTargetId}
        onClose={() => setDeleteTargetId(null)}
        onConfirm={() => deleteTargetId && deleteMutation.mutate(deleteTargetId)}
        isLoading={deleteMutation.isPending}
        title="Delete Review?"
        message="Are you sure you want to remove your review? This will also remove your rating from the provider's overall score."
        confirmLabel="Yes, Delete"
        cancelLabel="Keep Review"
        variant="danger"
      />
    </div>
  )
}
