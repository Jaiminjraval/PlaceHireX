import { motion } from "framer-motion";

/* ────────────────────────────────────────────────────────────────────
 * Reusable animation wrappers for the "Immersive Creative Studio" UI.
 * Import individual named exports as needed.
 * ─────────────────────────────────────────────────────────────────── */

/**
 * Fade-in + slide-up reveal for text blocks & headings.
 * @param {{ children, delay, className }} props
 */
export function TextReveal({ children, delay = 0, className = "" }) {
    return (
        <motion.div
            initial={{ opacity: 0, y: 18 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay, ease: "easeOut" }}
            className={className}
        >
            {children}
        </motion.div>
    );
}

/**
 * Fade-in + slight scale for cards.
 * @param {{ children, delay, className }} props
 */
export function CardReveal({ children, delay = 0, className = "" }) {
    return (
        <motion.div
            initial={{ opacity: 0, y: 24, scale: 0.97 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            transition={{ duration: 0.5, delay, ease: "easeOut" }}
            className={className}
        >
            {children}
        </motion.div>
    );
}

/**
 * Full-page wrapper with a simple fade transition.
 * Wrap the root div of each page.
 */
export function PageTransition({ children, className = "" }) {
    return (
        <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.4, ease: "easeInOut" }}
            className={className}
        >
            {children}
        </motion.div>
    );
}

/**
 * Continuously drifting background blob.
 * Uses endless keyframe loop so the blob slowly wanders.
 *
 * @param {{ className, duration }} props
 *   className – Tailwind classes for size, colour, blur, position
 *   duration  – Full loop duration in seconds (default 20)
 */
export function DriftingBlob({ className = "", duration = 20 }) {
    return (
        <motion.div
            className={className}
            animate={{
                x: [0, 30, -20, 10, 0],
                y: [0, -25, 15, -10, 0],
                scale: [1, 1.05, 0.97, 1.02, 1],
            }}
            transition={{
                duration,
                repeat: Infinity,
                repeatType: "loop",
                ease: "easeInOut",
            }}
        />
    );
}
