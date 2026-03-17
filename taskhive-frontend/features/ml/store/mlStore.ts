import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface MlState {
  isMlEnabled: boolean;
  setMlEnabled: (enabled: boolean) => void;
  toggleMlEnabled: () => void;
}

export const useMlStore = create<MlState>()(
  persist(
    (set) => ({
      isMlEnabled: false,
      setMlEnabled: (enabled: boolean) => set({ isMlEnabled: enabled }),
      toggleMlEnabled: () => set((state) => ({ isMlEnabled: !state.isMlEnabled })),
    }),
    {
      name: 'taskhive-ml-settings',
    }
  )
);
