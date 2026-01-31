import { ref } from 'vue'

export type ExportType = 'COA' | 'RULES' | 'NUMSCRIPT'

export interface ExportResponse {
  sessionId: number
  exportType: ExportType
  artifactId?: number
  content?: string
  success: boolean
  hasConflicts: boolean
  conflicts?: ExportConflictResponse[]
  message?: string
  exportedAt?: string
}

export interface ExportConflictResponse {
  exportType: ExportType
  hasConflicts: boolean
  conflictCount: number
  details: string[]
}

export function useExport() {
  const config = useRuntimeConfig()
  const apiBase = config.public.apiBase

  const exporting = ref(false)
  const exportError = ref<string | null>(null)

  async function exportDesign(sessionId: number, type: ExportType, force = false): Promise<ExportResponse> {
    exporting.value = true
    exportError.value = null
    
    try {
      const response = await fetch(`${apiBase}/sessions/${sessionId}/export/${type}?force=${force}`, {
        method: 'POST',
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Export failed')
      }
      
      return await response.json()
    } catch (error) {
      exportError.value = (error as Error).message
      throw error
    } finally {
      exporting.value = false
    }
  }

  async function getExportHistory(sessionId: number): Promise<ExportResponse[]> {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/export`)
    if (!response.ok) throw new Error('Failed to fetch export history')
    return await response.json()
  }

  async function previewConflicts(sessionId: number, type: ExportType): Promise<ExportConflictResponse> {
    const response = await fetch(`${apiBase}/sessions/${sessionId}/export/${type}/conflicts`)
    if (!response.ok) throw new Error('Failed to preview conflicts')
    return await response.json()
  }

  return {
    exporting,
    exportError,
    exportDesign,
    getExportHistory,
    previewConflicts,
  }
}
