import { useEffect, useRef } from "react";

/**
 * Reusable circular progress indicator with animated SVG stroke.
 *
 * @param {{ percentage: number }} props
 *   percentage – 0‑100 value displayed in the centre and used for the arc.
 */
export default function CircularProgress({ percentage = 0 }) {
    const circleRef = useRef(null);

    // ── Colour rules (pastel / muted) ──────────────────────────────────
    const getColor = (pct) => {
        if (pct >= 70) return "#86EFAC"; // pastel green
        if (pct >= 40) return "#FDE68A"; // pastel yellow
        return "#FCA5A5";                // pastel red
    };

    const color = getColor(percentage);

    // SVG geometry
    const size = 120;
    const strokeWidth = 10;
    const radius = (size - strokeWidth) / 2;
    const circumference = 2 * Math.PI * radius;
    const offset = circumference - (percentage / 100) * circumference;

    // Animate on mount / percentage change
    useEffect(() => {
        const el = circleRef.current;
        if (!el) return;
        // Start fully hidden, then transition to target offset
        el.style.transition = "none";
        el.style.strokeDashoffset = `${circumference}`;
        // Force reflow so the browser registers the start value
        void el.getBoundingClientRect();
        el.style.transition = "stroke-dashoffset 0.8s ease-out";
        el.style.strokeDashoffset = `${offset}`;
    }, [percentage, circumference, offset]);

    return (
        <div className="relative inline-flex items-center justify-center">
            <svg
                width={size}
                height={size}
                className="-rotate-90"
                viewBox={`0 0 ${size} ${size}`}
            >
                {/* Track */}
                <circle
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    fill="none"
                    stroke="#e5e7eb"
                    strokeWidth={strokeWidth}
                />

                {/* Progress arc */}
                <circle
                    ref={circleRef}
                    cx={size / 2}
                    cy={size / 2}
                    r={radius}
                    fill="none"
                    stroke={color}
                    strokeWidth={strokeWidth}
                    strokeLinecap="round"
                    strokeDasharray={circumference}
                    strokeDashoffset={circumference}
                />
            </svg>

            {/* Centre label */}
            <span className="absolute text-2xl font-extrabold tracking-tight text-black font-['Inter',sans-serif]">
                {Math.round(percentage)}%
            </span>
        </div>
    );
}
