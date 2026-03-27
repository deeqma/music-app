import { useMemo } from 'react'

interface IconProps {
  src: string   // raw SVG string — import with `?raw`
  size?: number
  color?: 'primary' | 'secondary' | 'accent'
  alt?: string
}

const CSS_VARS: Record<string, string> = {
  primary:   '--color-text-primary',
  secondary: '--color-text-secondary',
  accent:    '--color-accent',
}

function processColors(raw: string, cssVar: string): string {
  const value = `var(${cssVar})`
  return raw
    .replace(/\bfill="(?!none")[^"]*"/g,   `fill="${value}"`)
    .replace(/\bstroke="(?!none")[^"]*"/g, `stroke="${value}"`)
}

export default function Icon({ src, size = 20, color = 'primary', alt = '' }: IconProps) {
  const cssVar = CSS_VARS[color]
  const html = useMemo(() => processColors(src, cssVar), [src, cssVar])

  return (
    <span
      role="img"
      aria-label={alt}
      className="icon"
      style={{ width: size, height: size }}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  )
}
