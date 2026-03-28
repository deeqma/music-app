import { createPortal } from 'react-dom'
import { useNavigate } from 'react-router-dom'
import Icon from './Icon'
import xRaw from '../assets/x.svg?raw'

interface LoginPromptModalProps {
  onClose: () => void
}

export default function LoginPromptModal({ onClose }: LoginPromptModalProps) {
  const navigate = useNavigate()

  return createPortal(
    <div
      className="import-overlay"
      onClick={e => { if (e.target === e.currentTarget) onClose() }}
    >
      <div className="import-modal cpl-modal login-prompt-modal">
        <div className="import-modal__header">
          <span className="import-modal__title">Login required</span>
          <button className="import-modal__close" onClick={onClose} aria-label="Close">
            <Icon src={xRaw} size={16} color="secondary" alt="close" />
          </button>
        </div>
        <div className="import-modal__body login-prompt-modal__body">
          <p className="login-prompt-modal__text">Log in to use this feature.</p>
        </div>
        <div className="import-modal__footer cpl-modal__footer--row">
          <button
            type="button"
            className="import-modal__import-btn cpl-modal__cancel-btn"
            onClick={onClose}
          >
            Cancel
          </button>
          <button
            type="button"
            className="import-modal__import-btn"
            onClick={() => navigate('/login')}
          >
            Log in
          </button>
        </div>
      </div>
    </div>,
    document.body
  )
}
