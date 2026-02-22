import { useEffect } from "react";
import { motion, useMotionValue, useSpring } from "framer-motion";

/**
 * Solid black circle that smoothly follows the mouse pointer.
 * Hides the system cursor and renders a custom one instead.
 *
 * Mount this component once at the app root.
 */
export default function CustomCursor() {
    const cursorX = useMotionValue(-40);
    const cursorY = useMotionValue(-40);

    const springX = useSpring(cursorX, { damping: 25, stiffness: 250 });
    const springY = useSpring(cursorY, { damping: 25, stiffness: 250 });

    useEffect(() => {
        const move = (e) => {
            cursorX.set(e.clientX);
            cursorY.set(e.clientY);
        };
        window.addEventListener("mousemove", move);
        return () => window.removeEventListener("mousemove", move);
    }, [cursorX, cursorY]);

    return (
        <>
            {/* Hide default cursor globally */}
            <style>{`*, *::before, *::after { cursor: none !important; }`}</style>

            <motion.div
                className="pointer-events-none fixed top-0 left-0 z-[9999] h-5 w-5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-black"
                style={{ x: springX, y: springY }}
            />
        </>
    );
}
