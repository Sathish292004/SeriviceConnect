import { useState, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Camera, Upload, Trash2, X, Loader2, Image as ImageIcon } from 'lucide-react'
import { providerApi } from '@/api/provider'
import { LoadingState, ErrorState, EmptyState } from '@/components/shared/UxStates'

export default function ProviderPhotos() {
  const qc = useQueryClient()
  const fileInputRef = useRef<HTMLInputElement>(null)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [previewUrl, setPreviewUrl] = useState<string | null>(null)

  const { data: provider } = useQuery({
    queryKey: ['provider', 'me'],
    queryFn: () => providerApi.getMe(),
    select: (r) => r.data,
  })

  const { data: photos, isLoading, error } = useQuery({
    queryKey: ['provider', 'photos', provider?.id],
    queryFn: () => providerApi.getMyPhotos(provider!.id),
    select: (r) => r.data,
    enabled: !!provider,
  })

  const clearSelection = () => {
    setSelectedFile(null)
    if (previewUrl) {
      URL.revokeObjectURL(previewUrl)
      setPreviewUrl(null)
    }
    if (fileInputRef.current) {
      fileInputRef.current.value = ''
    }
  }

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    const allowedTypes = ['image/jpeg', 'image/png', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      toast.error('Only JPG, PNG, and WEBP images are supported')
      clearSelection()
      return
    }

    const maxSize = 5 * 1024 * 1024 // 5MB
    if (file.size > maxSize) {
      toast.error('Image file size must be less than 5MB')
      clearSelection()
      return
    }

    setSelectedFile(file)
    setPreviewUrl(URL.createObjectURL(file))
  }

  const uploadMutation = useMutation({
    mutationFn: async () => {
      if (!provider?.id || !selectedFile) {
        throw new Error('No provider or file selected')
      }
      return providerApi.uploadPhoto(provider.id, selectedFile, (photos?.length ?? 0) + 1)
    },
    onSuccess: () => {
      toast.success('Photo uploaded successfully')
      clearSelection()
      qc.invalidateQueries({ queryKey: ['provider', 'photos'] })
    },
    onError: (err: any) => {
      const msg = err?.response?.data?.message || err?.message || 'Failed to upload photo'
      toast.error(msg)
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (photoId: number) => providerApi.deletePhoto(provider!.id, photoId),
    onSuccess: () => {
      toast.success('Photo removed')
      qc.invalidateQueries({ queryKey: ['provider', 'photos'] })
    },
    onError: () => toast.error('Failed to remove photo'),
  })

  const formatFileSize = (bytes: number) => {
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[#0F172A]">Portfolio Photos</h1>
          <p className="text-sm text-[#64748B] mt-0.5">
            Upload photos of your completed projects and past work to showcase on your public profile.
          </p>
        </div>
        {photos && photos.length > 0 && (
          <span className="text-xs font-medium bg-[#F1F5F9] text-[#475569] px-3 py-1.5 rounded-full">
            {photos.length} photo{photos.length !== 1 ? 's' : ''}
          </span>
        )}
      </div>

      {/* Direct Image Upload Box */}
      <div className="sc-card p-6 border-2 border-dashed border-[#CBD5E1] hover:border-[#2563EB] transition-colors rounded-[16px] bg-[#F8FAFC]">
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={handleFileChange}
          className="hidden"
          id="provider-photo-file-input"
        />

        {!selectedFile ? (
          <div className="flex flex-col items-center justify-center text-center py-6">
            <div className="w-14 h-14 rounded-full bg-[#EFF6FF] flex items-center justify-center text-[#2563EB] mb-3">
              <Camera className="w-7 h-7" />
            </div>
            <h3 className="text-base font-semibold text-[#0F172A] mb-1">Upload portfolio photo</h3>
            <p className="text-xs text-[#64748B] mb-4">PNG, JPG, or WEBP up to 5MB</p>
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              className="sc-btn-primary text-sm px-5 py-2 inline-flex items-center gap-2 cursor-pointer"
            >
              <Upload className="w-4 h-4" /> Choose Image
            </button>
          </div>
        ) : (
          <div className="flex flex-col sm:flex-row items-center gap-5">
            {previewUrl && (
              <div className="relative flex-shrink-0 w-36 h-36 rounded-[12px] overflow-hidden border border-[#CBD5E1] bg-white shadow-sm">
                <img src={previewUrl} alt="Preview" className="w-full h-full object-cover" />
              </div>
            )}
            <div className="flex-1 min-w-0 text-center sm:text-left">
              <div className="flex items-center gap-2 justify-center sm:justify-start">
                <ImageIcon className="w-4 h-4 text-[#2563EB] flex-shrink-0" />
                <p className="text-sm font-semibold text-[#0F172A] truncate max-w-xs sm:max-w-md">
                  {selectedFile.name}
                </p>
              </div>
              <p className="text-xs text-[#64748B] mt-1">
                Size: {formatFileSize(selectedFile.size)} • {selectedFile.type.split('/')[1]?.toUpperCase() || 'IMAGE'}
              </p>
              <div className="flex items-center gap-3 mt-4 justify-center sm:justify-start">
                <button
                  type="button"
                  onClick={() => uploadMutation.mutate()}
                  disabled={uploadMutation.isPending}
                  className="sc-btn-primary text-sm px-4 py-2 inline-flex items-center gap-1.5 cursor-pointer"
                >
                  {uploadMutation.isPending ? (
                    <>
                      <Loader2 className="w-4 h-4 animate-spin" /> Uploading...
                    </>
                  ) : (
                    <>
                      <Upload className="w-4 h-4" /> Upload Photo
                    </>
                  )}
                </button>
                <button
                  type="button"
                  onClick={clearSelection}
                  disabled={uploadMutation.isPending}
                  className="sc-btn-outline text-sm px-4 py-2 inline-flex items-center gap-1 text-[#64748B] hover:text-[#EF4444] cursor-pointer"
                >
                  <X className="w-4 h-4" /> Cancel
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Portfolio Grid */}
      <div>
        <h2 className="text-lg font-semibold text-[#0F172A] mb-3">Your Portfolio</h2>
        {isLoading ? (
          <LoadingState message="Loading photos..." />
        ) : error ? (
          <ErrorState message="Failed to load portfolio photos" />
        ) : !photos?.length ? (
          <EmptyState title="No photos yet" message="Upload photos from your computer to showcase your past work." />
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
            {photos.map((p) => (
              <div key={p.id} className="relative group rounded-[12px] overflow-hidden border border-[#E2E8F0] shadow-sm bg-white">
                <img
                  src={p.imageUrl}
                  alt="Portfolio"
                  className="w-full h-44 object-cover transition-transform duration-300 group-hover:scale-105"
                />
                <div className="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors pointer-events-none" />
                <button
                  type="button"
                  data-testid="delete-photo-btn"
                  onClick={() => {
                    if (confirm('Are you sure you want to remove this photo?')) {
                      deleteMutation.mutate(p.id)
                    }
                  }}
                  disabled={deleteMutation.isPending}
                  aria-label="Remove photo"
                  className="absolute top-2 right-2 w-8 h-8 bg-white/95 hover:bg-white text-[#EF4444] rounded-full shadow-md flex items-center justify-center opacity-90 hover:opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-all hover:scale-110 cursor-pointer"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
