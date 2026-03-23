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
      setMlEnabled: (enabled: boolean) => set({ isMlEnabled: enabled }),
      toggleMlEnabled: () => set((state) => ({ isMlEnabled: !state.isMlEnabled })),
      
      isGeminiEnabled: true,
      setGeminiEnabled: (enabled: boolean) => set({ isGeminiEnabled: enabled }),
      toggleGeminiEnabled: () => set((state) => ({ isGeminiEnabled: !state.isGeminiEnabled })),
    }),
    {
      name: 'taskhive-ml-settings',
    }
  )
);
