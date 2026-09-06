/**
 * APPLE LIQUID GLASS & SPATIAL INTERACTION ENGINE (visionOS / iOS 18)
 * - Zero-Lag requestAnimationFrame (rAF) Performance Throttle
 * - VengeanceUI: Dynamic Spotlight Cursor Tracking on Navbar
 * - VengeanceUI + Skiper52: Harmonized 3D Perspective Tilt & Fluid Hover Elevation
 * - Skiper-UI: @skiper-ui/skiper52 Fluid Hover & Stagger Cascade Reveal
 * - Skiper-UI: @skiper-ui/skiper31 Tactile Micro-Interactions & Spring Press
 * - Apple iOS 18 System Semantic Palette for Chart.js Analytics
 * - Room Board Seamless Floor Grid <-> Table Mode Toggle
 * - 1-Click Demo Credentials Autofill
 */

document.addEventListener('DOMContentLoaded', () => {
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    // 1. VENGEANCE-UI: SPOTLIGHT NAVBAR TRACKING (THROTTLED WITH rAF)
    initSpotlightNavbar();

    // 2. VENGEANCE-UI + SKIPER52: 3D PERSPECTIVE TILT & FLUID HOVER (ZERO-LAG rAF)
    if (!reduceMotion) {
        init3DTiltAndSpecularShine();
    }

    // 3. VENGEANCE-UI: CENTER-OUT STAGGERED GRID REVEAL
    initVengeanceStaggeredGrid();

    // 4. NUMERIC KPI COUNT-UP ANIMATION (SNAPPY 450ms)
    initNumericCountUp(reduceMotion);

    // 5. ROOM BOARD VIEW TOGGLE (Floor Grid <-> Table)
    initRoomViewToggle();

    // 6. QUICK DEMO LOGIN AUTOFILL HELPER
    initDemoLoginAutofill();

    // 7. VENGEANCE-UI: ANIMATED LIQUID GLASS FOOTER CANVAS (DEFERRED TO PREVENT TAB LOAD STALL)
    setTimeout(initAnimatedFooterCanvas, 16);

    // 8. CHART.JS VISUAL ANALYTICS (Apple Neon Palette)
    initVisualCharts();

    // 9. SKIPER-UI: @skiper-ui/skiper106 SMOOTH CARET INPUT (ALL TYPING FUNCTIONS)
    initSmoothCaretInputs();

    // 10. SKIPER-UI: @skiper-ui/skiper87 SCROLL WITH FADE EFFECT & PROGRESS
    initScrollAnimations();

    // 11. SKIPER-UI: @skiper-ui/skiper103 BOUNCY ACCORDION (SPRING-PHYSICS INTERACTIVE)
    initBouncyAccordion();

    // 12. OMNIPRESENT SCROLL MOTION & FLUID VELOCITY ENGINE (EVERYTHING REACTS TO SCROLL)
    initOmnipresentScrollMotion();

    // 13. INTERACTIVE ARCHITECTURAL CANVAS POINTER BEAM
    window.addEventListener('mousemove', (e) => {
        document.body.style.setProperty('--mouse-x', `${e.clientX}px`);
        document.body.style.setProperty('--mouse-y', `${e.clientY}px`);
    }, { passive: true });
});

/**
 * VengeanceUI: Spotlight Navbar (@Ashutoshx7/VengeanceUI)
 * - Direct mouse tracking for immediate cursor spotlight response (--spotlight-x)
 * - Persistent active-item ambient luminous beam (--ambience-x)
 * - Physics spring return to the active tab on mouseleave
 */
function initSpotlightNavbar() {
    const nav = document.getElementById('spotlightNav');
    if (!nav) return;

    const activeItem = nav.querySelector('.spotlight-nav-link.active') || nav.querySelector('.spotlight-nav-link');
    
    let currentSpotlightX = 0;
    let targetSpotlightX = 0;
    let velocity = 0;
    let isHovered = false;
    let springRafId = null;

    function getActiveCenter() {
        if (!activeItem) return nav.offsetWidth / 2;
        const navRect = nav.getBoundingClientRect();
        const itemRect = activeItem.getBoundingClientRect();
        return itemRect.left - navRect.left + itemRect.width / 2;
    }

    function updatePositions() {
        const center = getActiveCenter();
        nav.style.setProperty('--ambience-x', `${center}px`);
        if (!isHovered) {
            targetSpotlightX = center;
            currentSpotlightX = center;
            velocity = 0;
            nav.style.setProperty('--spotlight-x', `${center}px`);
        }
    }

    // Spring physics simulation (stiffness: 200, damping: 20 equivalent)
    function runSpring() {
        if (isHovered) return;

        const k = 0.18; // Spring stiffness
        const d = 0.78; // Damping factor
        const force = (targetSpotlightX - currentSpotlightX) * k;
        velocity = (velocity + force) * d;
        currentSpotlightX += velocity;

        nav.style.setProperty('--spotlight-x', `${currentSpotlightX}px`);

        if (Math.abs(targetSpotlightX - currentSpotlightX) > 0.1 || Math.abs(velocity) > 0.05) {
            springRafId = requestAnimationFrame(runSpring);
        } else {
            currentSpotlightX = targetSpotlightX;
            nav.style.setProperty('--spotlight-x', `${currentSpotlightX}px`);
            springRafId = null;
        }
    }

    nav.addEventListener('mousemove', (e) => {
        isHovered = true;
        if (springRafId) {
            cancelAnimationFrame(springRafId);
            springRafId = null;
        }
        nav.classList.add('has-hover');
        const rect = nav.getBoundingClientRect();
        const x = e.clientX - rect.left;
        currentSpotlightX = x;
        velocity = 0;
        nav.style.setProperty('--spotlight-x', `${x}px`);
    }, { passive: true });

    nav.addEventListener('mouseleave', () => {
        isHovered = false;
        nav.classList.remove('has-hover');
        targetSpotlightX = getActiveCenter();
        if (!springRafId) {
            springRafId = requestAnimationFrame(runSpring);
        }
    });

    // Run initial setup after layout settles
    requestAnimationFrame(updatePositions);
    setTimeout(updatePositions, 50);
    window.addEventListener('resize', updatePositions, { passive: true });

    // Sticky Nav Scroll state & Dynamic Breadcrumb Reveal (@skiper-ui/skiper57)
    const stickyNav = document.getElementById('hkNavSticky') || nav.closest('.hk-nav-sticky') || document.querySelector('.hk-spotlight-header');
    if (stickyNav) {
        let wasScrolled = false;
        const handleStickyScroll = () => {
            const isScrolled = window.scrollY > 25;
            if (isScrolled !== wasScrolled) {
                wasScrolled = isScrolled;
                if (isScrolled) {
                    stickyNav.classList.add('is-scrolled', 'scrolled');
                } else {
                    stickyNav.classList.remove('is-scrolled', 'scrolled');
                }
                // Recalculate spotlight and ambience center after breadcrumb expansion transition completes
                setTimeout(updatePositions, 160);
                setTimeout(updatePositions, 380);
            }
        };
        window.addEventListener('scroll', handleStickyScroll, { passive: true });
        handleStickyScroll();
    }
}

/**
 * Clean VisionOS Flat-Glass: Gentle Hover Elevation & Specular Reflection
 * Eliminates aggressive 3D perspective tilt and heavy shadows.
 * Applies a smooth, subtle -3px vertical hover lift with responsive specular light catch.
 */
function init3DTiltAndSpecularShine() {
    const cards = document.querySelectorAll('.stat-card, .hk-room-tile, .hk-card-tilt, .bento-card, .hk-card');
    
    cards.forEach((card) => {
        let isHovered = false;
        let rafId = null;

        card.addEventListener('mouseenter', () => {
            isHovered = true;
            card.style.setProperty('--hover-lift', '-3px');
        });

        card.addEventListener('mousemove', (e) => {
            if (!isHovered) return;
            if (rafId) return;

            rafId = requestAnimationFrame(() => {
                const rect = card.getBoundingClientRect();
                const x = e.clientX - rect.left;
                const y = e.clientY - rect.top;

                // Update specular refraction highlight coordinates
                card.style.setProperty('--mouse-x', `${x}px`);
                card.style.setProperty('--mouse-y', `${y}px`);
                rafId = null;
            });
        }, { passive: true });

        card.addEventListener('mouseleave', () => {
            isHovered = false;
            if (rafId) {
                cancelAnimationFrame(rafId);
                rafId = null;
            }
            card.style.setProperty('--hover-lift', '0px');
        });
    });
}

/**
 * VengeanceUI: Dynamic Center-Out Staggered Grid Reveal
 * Analyzes grid column geometry and applies center-out staggered reveal delays:
 * delayFactor = Math.abs(colIndex - middleColumnIndex) * 0.055s + rowIndex * 0.045s
 */
function initVengeanceStaggeredGrid() {
    const grids = document.querySelectorAll('.hk-stats-row, .hk-room-grid, .hk-staggered-grid');
    grids.forEach((grid) => {
        const items = Array.from(grid.children).filter(el => el.nodeType === 1 && !el.classList.contains('grid-column-all'));
        if (items.length === 0) return;

        // Group elements into rows based on offsetTop
        const rows = [];
        let currentRow = [];
        let lastTop = -1;

        items.forEach(item => {
            const top = item.offsetTop;
            if (lastTop === -1 || Math.abs(top - lastTop) < 20) {
                currentRow.push(item);
                lastTop = top;
            } else {
                rows.push(currentRow);
                currentRow = [item];
                lastTop = top;
            }
        });
        if (currentRow.length > 0) rows.push(currentRow);

        // Compute center-out delay for each row
        rows.forEach((rowItems, rowIndex) => {
            const middleColIndex = (rowItems.length - 1) / 2;
            rowItems.forEach((item, colIndex) => {
                const centerDistance = Math.abs(colIndex - middleColIndex);
                const delay = (rowIndex * 0.04) + (centerDistance * 0.05);
                item.style.animation = 'vengeanceStaggerReveal 0.30s cubic-bezier(0.16, 1, 0.3, 1) both';
                item.style.animationDelay = `${delay.toFixed(3)}s`;
                item.setAttribute('data-stagger', 'true');
            });
        });
    });
}

/**
 * Numeric KPI Count-Up with Snappy Cubic Easing (450ms)
 */
function initNumericCountUp(reduceMotion) {
    document.querySelectorAll('.stat-number').forEach((el) => {
        const rawText = el.textContent.trim().replace(/[^0-9]/g, '');
        const target = parseInt(rawText, 10);
        if (reduceMotion || Number.isNaN(target) || target === 0) return;

        const hasPercent = el.textContent.includes('%');
        const duration = 450; // Snappy completion
        const start = performance.now();
        el.textContent = hasPercent ? '0%' : '0';

        const step = (now) => {
            const progress = Math.min((now - start) / duration, 1);
            const eased = 1 - Math.pow(1 - progress, 3); // Cubic ease-out
            const current = Math.round(target * eased);
            el.textContent = hasPercent ? `${current}%` : `${current}`;
            if (progress < 1) {
                requestAnimationFrame(step);
            } else {
                el.textContent = hasPercent ? `${target}%` : `${target}`;
            }
        };
        requestAnimationFrame(step);
    });
}

/**
 * Room Board View Toggle (Floor Grid <-> Table)
 */
function initRoomViewToggle() {
    const gridViewBtn = document.getElementById('btnGridView');
    const tableViewBtn = document.getElementById('btnTableView');
    const gridViewEl = document.getElementById('roomsGridView');
    const tableViewEl = document.getElementById('roomsTableView');

    if (gridViewBtn && tableViewBtn && gridViewEl && tableViewEl) {
        function setViewMode(mode) {
            if (mode === 'table') {
                gridViewEl.style.display = 'none';
                tableViewEl.style.display = 'block';
                tableViewBtn.classList.add('active');
                gridViewBtn.classList.remove('active');
                localStorage.setItem('hk_rooms_view_mode', 'table');
            } else {
                gridViewEl.style.display = 'grid';
                tableViewEl.style.display = 'none';
                gridViewBtn.classList.add('active');
                tableViewBtn.classList.remove('active');
                localStorage.setItem('hk_rooms_view_mode', 'grid');
            }
        }

        gridViewBtn.addEventListener('click', () => setViewMode('grid'));
        tableViewBtn.addEventListener('click', () => setViewMode('table'));

        const savedView = localStorage.getItem('hk_rooms_view_mode') || 'grid';
        setViewMode(savedView);
    }
}

/**
 * 1-Click Quick Demo Login Autofill
 */
function initDemoLoginAutofill() {
    const demoChips = document.querySelectorAll('.demo-chip');
    if (demoChips.length > 0) {
        demoChips.forEach((chip) => {
            chip.addEventListener('click', () => {
                const user = chip.getAttribute('data-user');
                const pass = chip.getAttribute('data-pass');
                const userInput = document.querySelector('input[name="username"]');
                const passInput = document.querySelector('input[name="password"]');
                if (userInput && passInput) {
                    userInput.value = user;
                    passInput.value = pass;
                    userInput.focus();
                    chip.style.transform = 'scale(0.92)';
                    setTimeout(() => chip.style.transform = '', 180);
                }
            });
        });
    }
}

/**
 * Chart.js Visual Analytics (Apple iOS 18 System Semantic Palette)
 */
function initVisualCharts() {
    if (typeof Chart === 'undefined') return;

    // Apple Dark mode typography defaults
    Chart.defaults.color = '#a1a1a6';
    Chart.defaults.font.family = '-apple-system, BlinkMacSystemFont, "SF Pro Text", "Sora", sans-serif';

    // A. Room Readiness Donut Chart
    const roomCanvas = document.getElementById('roomsDoughnutChart');
    if (roomCanvas) {
        const dirty = parseInt(roomCanvas.getAttribute('data-dirty') || '0', 10);
        const inProgress = parseInt(roomCanvas.getAttribute('data-inprogress') || '0', 10);
        const clean = parseInt(roomCanvas.getAttribute('data-clean') || '0', 10);
        const inspected = parseInt(roomCanvas.getAttribute('data-inspected') || '0', 10);

        new Chart(roomCanvas, {
            type: 'doughnut',
            data: {
                labels: ['Dirty', 'In Progress', 'Clean', 'Inspected'],
                datasets: [{
                    data: [dirty, inProgress, clean, inspected],
                    backgroundColor: [
                        '#f43f5e', // Refined Rose (Dirty)
                        '#f59e0b', // Warm Amber (In Progress)
                        '#10b981', // Emerald (Clean)
                        '#cbd5e1'  // Frosted Titanium (Inspected)
                    ],
                    borderColor: '#090c13',
                    borderWidth: 3,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '74%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 16,
                            usePointStyle: true,
                            boxWidth: 8,
                            color: '#e2e8f0'
                        }
                    }
                }
            }
        });
    }

    // B. Cleaning Tasks Distribution Bar Chart
    const tasksCanvas = document.getElementById('tasksBarChart');
    if (tasksCanvas) {
        const pending = parseInt(tasksCanvas.getAttribute('data-pending') || '0', 10);
        const inProg = parseInt(tasksCanvas.getAttribute('data-inprogress') || '0', 10);
        const completed = parseInt(tasksCanvas.getAttribute('data-completed') || '0', 10);

        new Chart(tasksCanvas, {
            type: 'bar',
            data: {
                labels: ['Pending', 'In Progress', 'Completed'],
                datasets: [{
                    label: 'Tasks',
                    data: [pending, inProg, completed],
                    backgroundColor: [
                        'rgba(255, 255, 255, 0.20)',
                        '#f59e0b',
                        '#10b981'
                    ],
                    borderColor: [
                        'rgba(255, 255, 255, 0.35)',
                        '#f59e0b',
                        '#10b981'
                    ],
                    borderWidth: 1,
                    borderRadius: 8,
                    maxBarThickness: 42
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: '#94a3b8' }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(255, 255, 255, 0.05)' },
                        ticks: { precision: 0, color: '#94a3b8' }
                    }
                }
            }
        });
    }

    // C. Maintenance Requests Breakdown Chart
    const maintCanvas = document.getElementById('maintenanceChart');
    if (maintCanvas) {
        const open = parseInt(maintCanvas.getAttribute('data-open') || '0', 10);
        const inProg = parseInt(maintCanvas.getAttribute('data-inprogress') || '0', 10);
        const resolved = parseInt(maintCanvas.getAttribute('data-resolved') || '0', 10);

        new Chart(maintCanvas, {
            type: 'doughnut',
            data: {
                labels: ['Open', 'In Progress', 'Resolved'],
                datasets: [{
                    data: [open, inProg, resolved],
                    backgroundColor: [
                        '#f43f5e', // Rose
                        '#f59e0b', // Amber
                        '#10b981'  // Emerald
                    ],
                    borderColor: '#090c13',
                    borderWidth: 3,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '72%',
                plugins: {
                    legend: {
                        position: 'bottom',
                        labels: {
                            padding: 16,
                            usePointStyle: true,
                            boxWidth: 8,
                            color: '#f5f5f7'
                        }
                    }
                }
            }
        });
    }
}

/**
 * VengeanceUI: Animated Liquid Glass Footer Interactive Canvas
 * Dynamic glyph clusters & luminous cursor tracking particles with auto-pause via IntersectionObserver.
 */
function initAnimatedFooterCanvas() {
    const canvas = document.getElementById('hkFooterCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const footer = canvas.closest('.hk-animated-footer');
    if (!footer) return;

    let width = 0;
    let height = 0;
    let dpr = window.devicePixelRatio || 1;
    let animationFrameId = null;
    let isVisible = false;

    // Interactive cursor tracking
    const mouse = {
        x: -9999,
        y: -9999,
        active: false,
        radius: 170
    };

    // Micro-glyph characters matching VengeanceUI aesthetic
    const glyphs = ['+', '·', '•', '×', '✦', '°'];
    
    let particles = [];
    const GRID_STEP = 38; // Spacing of ambient glyph lattice

    function resize() {
        const rect = footer.getBoundingClientRect();
        width = rect.width;
        height = rect.height;
        dpr = Math.min(window.devicePixelRatio || 1, 2);

        canvas.width = Math.floor(width * dpr);
        canvas.height = Math.floor(height * dpr);
        ctx.setTransform(dpr, 0, 0, dpr, 0, 0);

        // Rebuild particle grid
        particles = [];
        const cols = Math.ceil(width / GRID_STEP);
        const rows = Math.ceil(height / GRID_STEP);

        for (let r = 0; r <= rows; r++) {
            for (let c = 0; c <= cols; c++) {
                const charIndex = (r * 3 + c) % glyphs.length;
                particles.push({
                    x: c * GRID_STEP + (r % 2 === 0 ? 0 : GRID_STEP / 2),
                    y: r * GRID_STEP,
                    char: glyphs[charIndex],
                    baseAlpha: 0.08,
                    alpha: 0.08,
                    targetAlpha: 0.08
                });
            }
        }
    }

    resize();
    window.addEventListener('resize', resize, { passive: true });

    footer.addEventListener('mousemove', (e) => {
        const rect = footer.getBoundingClientRect();
        mouse.x = e.clientX - rect.left;
        mouse.y = e.clientY - rect.top;
        mouse.active = true;
    }, { passive: true });

    footer.addEventListener('mouseleave', () => {
        mouse.active = false;
        mouse.x = -9999;
        mouse.y = -9999;
    });

    function render() {
        if (!isVisible) return;

        ctx.clearRect(0, 0, width, height);

        const time = performance.now() * 0.0012;

        ctx.font = '11px -apple-system, BlinkMacSystemFont, "JetBrains Mono", monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';

        for (let i = 0; i < particles.length; i++) {
            const p = particles[i];

            let proximity = 0;
            if (mouse.active) {
                const dx = mouse.x - p.x;
                const dy = mouse.y - p.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < mouse.radius) {
                    proximity = 1 - (dist / mouse.radius);
                }
            }

            // Shimmering ambient glow
            const shimmer = Math.sin(time + p.x * 0.02 + p.y * 0.02) * 0.035;
            p.targetAlpha = Math.max(p.baseAlpha + shimmer, proximity * 0.9);
            p.alpha += (p.targetAlpha - p.alpha) * 0.22; // Snappy 0ms-lag easing

            if (p.alpha > 0.02) {
                if (proximity > 0.35) {
                    // Apple Liquid Cyan / Vengeance Glow
                    ctx.fillStyle = `rgba(0, 199, 190, ${p.alpha})`;
                    ctx.shadowColor = 'rgba(0, 199, 190, 0.7)';
                    ctx.shadowBlur = 8;
                } else if (proximity > 0.08) {
                    // High-reflect Frosted White
                    ctx.fillStyle = `rgba(255, 255, 255, ${p.alpha * 1.25})`;
                    ctx.shadowColor = 'rgba(255, 255, 255, 0.45)';
                    ctx.shadowBlur = 4;
                } else {
                    ctx.fillStyle = `rgba(255, 255, 255, ${p.alpha})`;
                    ctx.shadowColor = 'transparent';
                    ctx.shadowBlur = 0;
                }

                ctx.fillText(p.char, p.x, p.y);
            }
        }

        // Ambient radial light under cursor
        if (mouse.active) {
            const gradient = ctx.createRadialGradient(mouse.x, mouse.y, 0, mouse.x, mouse.y, mouse.radius);
            gradient.addColorStop(0, 'rgba(0, 199, 190, 0.14)');
            gradient.addColorStop(0.5, 'rgba(255, 255, 255, 0.035)');
            gradient.addColorStop(1, 'rgba(0, 0, 0, 0)');

            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(mouse.x, mouse.y, mouse.radius, 0, Math.PI * 2);
            ctx.fill();
        }

        animationFrameId = requestAnimationFrame(render);
    }

    // IntersectionObserver: Only animates when footer is visible on screen
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                isVisible = true;
                if (!animationFrameId) {
                    animationFrameId = requestAnimationFrame(render);
                }
            } else {
                isVisible = false;
                if (animationFrameId) {
                    cancelAnimationFrame(animationFrameId);
                    animationFrameId = null;
                }
            }
        });
    }, { threshold: 0.05 });

    observer.observe(footer);
}

/**
 * Robust Room Filter Handler with Event Delegation Support
 */
function filterRooms(status, btnElement) {
    const activeBtn = btnElement || (typeof event !== 'undefined' && event ? (event.currentTarget || event.target.closest('.hk-filter-pill')) : null);
    if (activeBtn) {
        document.querySelectorAll('.hk-filter-pill').forEach(b => b.classList.remove('active'));
        activeBtn.classList.add('active');
    }

    const gridItems = document.querySelectorAll('#roomsGridView .hk-room-tile');
    const tableRows = document.querySelectorAll('#roomsTableView tbody tr');

    gridItems.forEach(card => {
        const cardStatus = card.getAttribute('data-status');
        if (status === 'ALL' || cardStatus === status) {
            card.style.display = 'flex';
        } else {
            card.style.display = 'none';
        }
    });

    tableRows.forEach(row => {
        const rowStatus = row.getAttribute('data-status');
        if (status === 'ALL' || rowStatus === status) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

/**
 * SKIPER-UI: @skiper-ui/skiper106 Smooth Caret Input
 * Replaces harsh native browser blinking cursor with a spring-interpolated luminous liquid glass caret
 * across all typing inputs and textareas.
 */
function initSmoothCaretInputs() {
    const inputs = document.querySelectorAll('input[type="text"], input[type="password"], textarea');
    if (inputs.length === 0) return;

    const measureCanvas = document.createElement('canvas');
    const measureCtx = measureCanvas.getContext('2d');

    inputs.forEach((input) => {
        if (input.dataset.smoothCaretInit) return;
        input.dataset.smoothCaretInit = 'true';

        // Wrap input in smooth-caret-wrapper if not already wrapped
        let wrapper = input.parentElement;
        if (!wrapper.classList.contains('smooth-caret-wrapper')) {
            wrapper = document.createElement('div');
            wrapper.className = 'smooth-caret-wrapper';
            input.parentNode.insertBefore(wrapper, input);
            wrapper.appendChild(input);
        }

        const caret = document.createElement('span');
        caret.className = 'smooth-caret';
        wrapper.appendChild(caret);

        function updateCaret() {
            if (document.activeElement !== input) {
                caret.classList.remove('active');
                return;
            }

            caret.classList.add('active');
            const style = window.getComputedStyle(input);
            measureCtx.font = `${style.fontWeight} ${style.fontSize} ${style.fontFamily}`;

            const val = input.value || '';
            const selStart = input.selectionStart || 0;
            const textBefore = val.substring(0, selStart);
            const textWidth = measureCtx.measureText(textBefore).width;

            const padLeft = parseFloat(style.paddingLeft) || 12;
            const padTop = parseFloat(style.paddingTop) || 10;
            const scrollLeft = input.scrollLeft || 0;

            const x = padLeft + textWidth - scrollLeft;
            const y = padTop;

            caret.style.transform = `translate3d(${Math.round(x)}px, ${Math.round(y)}px, 0)`;
            caret.style.height = `${Math.round(parseFloat(style.fontSize) * 1.2)}px`;
        }

        input.addEventListener('input', updateCaret, { passive: true });
        input.addEventListener('keydown', () => requestAnimationFrame(updateCaret), { passive: true });
        input.addEventListener('keyup', updateCaret, { passive: true });
        input.addEventListener('click', updateCaret, { passive: true });
        input.addEventListener('focus', () => {
            input.classList.add('typing-active');
            caret.classList.add('active');
            updateCaret();
        }, { passive: true });
        input.addEventListener('blur', () => {
            input.classList.remove('typing-active');
            caret.classList.remove('active');
        }, { passive: true });
        input.addEventListener('scroll', updateCaret, { passive: true });
    });
}

/**
 * SKIPER-UI: @skiper-ui/skiper87 Scroll with Fade Effect & Ambient Scroll Progress
 * - Fixed 3px Apple neon scroll progress indicator along top viewport edge
 * - Sticky navbar depth elevation upon scrolling past 25px
 * - Responsive dynamic edge fade masks on scrollable data tables
 */
function initScrollAnimations() {
    // 1. Ambient Top Scroll Progress Rail
    let progressBar = document.querySelector('.hk-scroll-progress');
    if (!progressBar) {
        progressBar = document.createElement('div');
        progressBar.className = 'hk-scroll-progress';
        document.body.appendChild(progressBar);
    }

    const stickyNavs = document.querySelectorAll('.hk-nav-sticky, .hk-navbar');
    let scrollRafId = null;

    function handleWindowScroll() {
        if (scrollRafId) return;
        scrollRafId = requestAnimationFrame(() => {
            const scrollTop = window.scrollY || document.documentElement.scrollTop;
            const docHeight = document.documentElement.scrollHeight - window.innerHeight;
            const progress = docHeight > 0 ? Math.min(100, Math.max(0, (scrollTop / docHeight) * 100)) : 0;
            progressBar.style.width = `${progress}%`;

            const isScrolled = scrollTop > 35;
            stickyNavs.forEach((nav) => {
                nav.classList.toggle('is-scrolled', isScrolled);
                nav.classList.toggle('scrolled', isScrolled);
            });
            scrollRafId = null;
        });
    }

    window.addEventListener('scroll', handleWindowScroll, { passive: true });
    handleWindowScroll(); // Initial run

    // 2. Dynamic Edge Fade Masks for Tables & Overflow Containers
    const scrollContainers = document.querySelectorAll('.table-responsive');
    scrollContainers.forEach((container) => {
        function updateTableMask() {
            const scrollLeft = container.scrollLeft;
            const maxScroll = container.scrollWidth - container.clientWidth;
            const fadeLeft = scrollLeft > 12;
            const fadeRight = scrollLeft < maxScroll - 12;

            if (fadeLeft && fadeRight) {
                container.style.webkitMaskImage = 'linear-gradient(to right, transparent 0, black 24px, black calc(100% - 24px), transparent 100%)';
            } else if (fadeLeft) {
                container.style.webkitMaskImage = 'linear-gradient(to right, transparent 0, black 24px, black 100%)';
            } else if (fadeRight) {
                container.style.webkitMaskImage = 'linear-gradient(to right, black 0%, black calc(100% - 24px), transparent 100%)';
            } else {
                container.style.webkitMaskImage = 'none';
            }
        }

        container.addEventListener('scroll', updateTableMask, { passive: true });
        updateTableMask();
    });
}

/**
 * SKIPER-UI: @skiper-ui/skiper103 Bouncy Accordion System
 * - Spring-physics expansion with cubic-bezier(0.34, 1.56, 0.64, 1)
 * - Accessible keyboard triggers (Enter / Space)
 * - Mutually exclusive or independent toggling via data-accordion-single
 * - Zero layout thrashing
 */
function initBouncyAccordion() {
    const headers = document.querySelectorAll('.bouncy-accordion-header');
    if (!headers.length) return;

    headers.forEach((header) => {
        const item = header.closest('.bouncy-accordion-item');
        if (!item) return;

        function toggleItem(e) {
            if (e) e.preventDefault();
            const isOpen = item.classList.contains('is-open');
            const parentAccordion = item.closest('.bouncy-accordion');
            const isSingleMode = parentAccordion && parentAccordion.hasAttribute('data-accordion-single');

            if (isSingleMode && !isOpen) {
                // Smoothly close siblings in the same single-mode accordion
                const siblings = parentAccordion.querySelectorAll('.bouncy-accordion-item.is-open');
                siblings.forEach((sibling) => {
                    if (sibling !== item) {
                        sibling.classList.remove('is-open');
                        const sibHeader = sibling.querySelector('.bouncy-accordion-header');
                        if (sibHeader) sibHeader.setAttribute('aria-expanded', 'false');
                    }
                });
            }

            if (isOpen) {
                item.classList.remove('is-open');
                header.setAttribute('aria-expanded', 'false');
            } else {
                item.classList.add('is-open');
                header.setAttribute('aria-expanded', 'true');
            }
        }

        header.addEventListener('click', toggleItem);
        header.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                toggleItem(e);
            }
        });
    });
}

/**
 * OMNIPRESENT SCROLL MOTION & FLUID VELOCITY ENGINE
 * - Tracks instantaneous scroll velocity (v = deltaY / deltaTime) with lerp smoothing
 * - Directly influences all viewport cards, stats, and accordions:
 *   1. Dynamic Momentum 3D Tilt: cards subtly pitch along the X-axis during scroll
 *   2. Velocity Scale Compression: subtle spring elastic feel during rapid scrolling
 *   3. Differential Spatial Depth: elements on different layers move at distinct parallax rates
 *   4. Specular Highlight Drift: moves light reflection across liquid glass edges
 * - Springs smoothly back to rest (0deg, scale 1) with Apple visionOS spring physics when scroll stops
 */
function initOmnipresentScrollMotion() {
    const reactiveElements = document.querySelectorAll('.stat-card, .hk-card, .hk-room-tile, .bouncy-accordion-item, .hk-header-row');
    if (!reactiveElements.length) return;

    let lastScrollY = window.scrollY || document.documentElement.scrollTop;
    let lastTime = performance.now();
    let currentVelocity = 0;
    let targetVelocity = 0;
    let scrollRafId = null;
    let stopTimeout = null;

    function onScroll() {
        const now = performance.now();
        const currentScrollY = window.scrollY || document.documentElement.scrollTop;
        const dt = Math.max(1, now - lastTime);
        const dy = currentScrollY - lastScrollY;

        // Raw velocity in px/ms, scaled for subtle, elegant physical motion
        const rawVelocity = (dy / dt) * 16; 
        targetVelocity = Math.max(-20, Math.min(20, rawVelocity));

        lastScrollY = currentScrollY;
        lastTime = now;

        if (!scrollRafId) {
            scrollRafId = requestAnimationFrame(updateScrollPhysics);
        }

        // Clear stopping timer
        if (stopTimeout) clearTimeout(stopTimeout);
        stopTimeout = setTimeout(() => {
            targetVelocity = 0;
        }, 80);
    }

    function updateScrollPhysics() {
        // Smooth lerp velocity towards target
        currentVelocity += (targetVelocity - currentVelocity) * 0.18;

        const isResting = Math.abs(currentVelocity) < 0.05 && Math.abs(targetVelocity) < 0.05;

        // Calculate pitch angle: scrolling down pitches cards forward, up pitches backward (capped at ±1.4deg)
        const pitchDeg = Math.max(-1.4, Math.min(1.4, currentVelocity * 0.12));
        // Subtle vertical compression scale at high velocity (0.992 to 1.0)
        const scaleVal = 1 - Math.min(0.008, Math.abs(currentVelocity) * 0.0005);

        const winH = window.innerHeight;

        reactiveElements.forEach((el) => {
            const rect = el.getBoundingClientRect();
            // Only update elements visible in or near viewport
            if (rect.bottom >= -100 && rect.top <= winH + 100) {
                // Viewport center parallax offset: items higher up or lower down have subtle differential depth
                const centerDiff = (winH / 2 - (rect.top + rect.height / 2)) / (winH / 2);
                const parallaxY = centerDiff * 6; // subtle ±6px depth parallax

                if (isResting) {
                    el.style.setProperty('--scroll-velocity-pitch', '0deg');
                    el.style.setProperty('--scroll-velocity-scale', '1');
                    el.style.setProperty('--scroll-parallax-y', `${parallaxY.toFixed(1)}px`);
                } else {
                    el.style.setProperty('--scroll-velocity-pitch', `${pitchDeg.toFixed(2)}deg`);
                    el.style.setProperty('--scroll-velocity-scale', scaleVal.toFixed(3));
                    el.style.setProperty('--scroll-parallax-y', `${parallaxY.toFixed(1)}px`);
                }
            }
        });

        if (!isResting) {
            scrollRafId = requestAnimationFrame(updateScrollPhysics);
        } else {
            scrollRafId = null;
        }
    }

    window.addEventListener('scroll', onScroll, { passive: true });
    // Initial run
    onScroll();
}
