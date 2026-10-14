import { QueryClient } from '@tanstack/react-query'

// Private data remains memory-only. Do not add a persisted-query plugin here.
export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 15_000,
      refetchOnWindowFocus: true,
      retry: 1,
    },
  },
})

export async function clearPrivateQueries(): Promise<void> {
  await queryClient.cancelQueries({ queryKey: ['private'] })
  queryClient.removeQueries({ queryKey: ['private'] })
}
