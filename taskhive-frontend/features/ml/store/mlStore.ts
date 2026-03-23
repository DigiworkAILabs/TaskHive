import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface MlState {
  isMlEnabled: boolean;
  setMlEnabled: (enabled: boolean) => void;
  toggleMlEnabled: () => void;
  isGeminiEnabled: boolean;
  setGeminiEnabled: (enabled: boolean) => void;
  toggleGeminiEnabled: () => void;
}

export const useMlStore = create<MlState>()(
  persist(
    (set) => ({
      isMlEnabled: false,
      setMlEnabled: (enabled: boolean) => set((state) => ({ 
          isMlEnabled: enabled, 
          isGeminiEnabled: enabled ? false : state.isGeminiEnabled 
      })),
      toggleMlEnabled: () => set((state) => {
          const next = !state.isMlEnabled;
          return { isMlEnabled: next, isGeminiEnabled: next ? false : state.isGeminiEnabled };
      }),
      
      isGeminiEnabled: true,
      setGeminiEnabled: (enabled: boolean) => set((state) => ({ 
          isGeminiEnabled: enabled, 
          isMlEnabled: enabled ? false : state.isMlEnabled 
      })),
      toggleGeminiEnabled: () => set((state) => {
          const next = !state.isGeminiEnabled;
          return { isGeminiEnabled: next, isMlEnabled: next ? false : state.isMlEnabled };
      }),
    }),
    {
      name: 'taskhive-ml-settings',
    }
  )
);
