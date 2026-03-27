import { useMemo } from 'react'

interface IconProps {
  readonly src: string   // raw SVG string — import with `?raw`
  readonly size?: number
  readonly color?: 'primary' | 'secondary' | 'accent'
  readonly alt?: string
}

const CSS_VARS: Record<string, string> = {
  primary:   '--color-text-primary',
  secondary: '--color-text-secondary',
  accent:    '--color-accent',
}

function processColors(raw: string, cssVar: string): string {
  const value = `var(${cssVar})`
  return raw
    .replaceAll(/\bfill="(?!none")[^"]*"/g,   `fill="${value}"`)
    .replaceAll(/\bstroke="(?!none")[^"]*"/g, `stroke="${value}"`)
}

export default function Icon({ src, size = 20, color = 'primary' }: IconProps) {
  const cssVar = CSS_VARS[color]
  const html = useMemo(() => processColors(src, cssVar), [src, cssVar])

  return (
    <span
      aria-hidden="true"
      className="icon"
      style={{ width: size, height: size }}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  )
}
