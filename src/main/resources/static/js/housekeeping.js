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
});

/**
 * VengeanceUI: Spotlight Navbar
 * Uses requestAnimationFrame to calculate cursor coordinates smoothly without GPU lag.
 */
function initSpotlightNavbar() {
    const navbar = document.querySelector('.hk-navbar');
    if (!navbar) return;

    let rafId = null;

    navbar.addEventListener('mousemove', (e) => {
        if (rafId) return;
        rafId = requestAnimationFrame(() => {
            const rect = navbar.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;
            navbar.style.setProperty('--spotlight-x', `${x}px`);
            navbar.style.setProperty('--spotlight-y', `${y}px`);
            rafId = null;
        });
    }, { passive: true });

    navbar.addEventListener('mouseleave', () => {
        if (rafId) cancelAnimationFrame(rafId);
        rafId = null;
    });
}

/**
 * VengeanceUI + Skiper52: 3D Perspective Tilt & Fluid Hover Elevation
 * Harmonizes 3D perspective tilt with Skiper52 hover elevation (-8px translateY + 1.035 scale)
 * Eliminates sluggish transition interpolation during active mouse tracking for true 0ms tactile response.
 */
function init3DTiltAndSpecularShine() {
    const tiltCards = document.querySelectorAll('.stat-card, .hk-room-tile, .hk-card-tilt, .bento-card');
    
    tiltCards.forEach((card) => {
        let isHovered = false;
        let rafId = null;

        card.addEventListener('mouseenter', () => {
            isHovered = true;
            card.style.willChange = 'transform, box-shadow';
            // Disable transition on transform so tracking is 100% instant and tactile
            card.style.transition = 'border-color 0.2s ease, box-shadow 0.2s ease';
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

                // Calculate 3D tilt angles (capped at ±6.5 degrees)
                const centerX = rect.width / 2;
                const centerY = rect.height / 2;
                const rotateX = ((y - centerY) / centerY) * -6.5;
                const rotateY = ((x - centerX) / centerX) * 6.5;

                // Combine 3D Tilt WITH Skiper52 Fluid Hover Elevation (-8px translateY & 1.035 scale)
                card.style.transform = `perspective(1000px) translateY(-8px) rotateX(${rotateX.toFixed(2)}deg) rotateY(${rotateY.toFixed(2)}deg) scale3d(1.035, 1.035, 1.035)`;
                rafId = null;
            });
        }, { passive: true });

        card.addEventListener('mouseleave', () => {
            isHovered = false;
            if (rafId) {
                cancelAnimationFrame(rafId);
                rafId = null;
            }
            // Smooth spring return to rest state with Apple visionOS spring
            card.style.transition = 'transform 0.38s cubic-bezier(0.16, 1, 0.3, 1), border-color 0.25s ease, box-shadow 0.25s ease';
            card.style.transform = 'perspective(1000px) translateY(0) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)';
            setTimeout(() => {
                if (!isHovered) card.style.willChange = 'auto';
            }, 400);
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
                        '#ff375f', // Apple Neon Coral
                        '#ff9f0a', // Apple Electric Amber
                        '#30d158', // Apple Spring Emerald
                        '#00c7be'  // Apple Liquid Cyan
                    ],
                    borderColor: '#06080e',
                    borderWidth: 4,
                    hoverOffset: 8
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
                        'rgba(255, 255, 255, 0.28)',
                        '#ff9f0a',
                        '#30d158'
                    ],
                    borderColor: [
                        'rgba(255, 255, 255, 0.45)',
                        '#ff9f0a',
                        '#30d158'
                    ],
                    borderWidth: 1,
                    borderRadius: 10,
                    maxBarThickness: 46
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
                        ticks: { color: '#a1a1a6' }
                    },
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(255, 255, 255, 0.06)' },
                        ticks: { precision: 0, color: '#a1a1a6' }
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
                        '#ff375f', // Coral
                        '#ff9f0a', // Amber
                        '#30d158'  // Emerald
                    ],
                    borderColor: '#06080e',
                    borderWidth: 4,
                    hoverOffset: 8
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
