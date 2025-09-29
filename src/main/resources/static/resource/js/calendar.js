function renderMiniCalendar(targetId, events, date = new Date()) {
	const container = document.getElementById(targetId);
	container.innerHTML = '';

	const year = date.getFullYear();
	const month = date.getMonth();

	const firstDay = new Date(year, month, 1).getDay();
	const lastDate = new Date(year, month + 1, 0).getDate();
	const prevLastDate = new Date(year, month, 0).getDate();
	const today = new Date();

	const englishMonthNames = [
		"JANUARY 1", "FEBRUARY 2", "MARCH 3", "APRIL 4", "MAY 5", "JUNE 6",
		"JULY 7", "AUGUST 8", "SEPTEMBER 9", "OCTOBER 10", "NOVEMBER 11", "DECEMBER 12"
	];

	const monthLabel = document.getElementById('current-month-label');
	if (monthLabel) {
		monthLabel.textContent = englishMonthNames[month];
		monthLabel.classList.add('font-bold');
	}
	const header = document.createElement('div');
	header.className = 'flex justify-between items-center mb-4 text-yellow-200';

	const prevBtn = document.createElement('button');
	prevBtn.innerHTML = '&lt;';
	prevBtn.className = 'text-black hover:text-yellow-800';

	const nextBtn = document.createElement('button');
	nextBtn.innerHTML = '&gt;';
	nextBtn.className = 'text-black hover:text-yellow-800';

	const title = document.createElement('h2');
	title.className = 'text-lg font-bold text-center text-black flex-1';
	title.textContent = year + '년 ' + (month + 1) + '월';

	prevBtn.onclick = function() {
		const newDate = new Date(year, month - 1, 1);
		renderMiniCalendar(targetId, events, newDate);
	};
	nextBtn.onclick = function() {
		const newDate = new Date(year, month + 1, 1);
		renderMiniCalendar(targetId, events, newDate);
	};



	header.appendChild(prevBtn);
	header.appendChild(title);
	header.appendChild(nextBtn);
	container.appendChild(header);

	const weekRow = document.createElement('div');

	weekRow.className = 'grid grid-cols-7 text-center text-gray-700 mb-1';
	['일', '월', '화', '수', '목', '금', '토'].forEach(function (day) {

		const div = document.createElement('div');
		div.textContent = day;
		weekRow.appendChild(div);
	});
	container.appendChild(weekRow);

	const grid = document.createElement('div');
	grid.className = 'grid grid-cols-7 grid-rows-6 gap-y-1 text-center';

	for (let i = firstDay - 1; i >= 0; i--) {
		const d = prevLastDate - i;
		const el = document.createElement('div');
		el.textContent = d;
		el.className = 'text-gray-400';
		grid.appendChild(el);
	}

	for (let i = 1; i <= lastDate; i++) {
		const el = document.createElement('div');
		el.className = 'relative cursor-pointer hover:bg-yellow-100';
		el.textContent = i;

		if (
			i === today.getDate() &&
			month === today.getMonth() &&
			year === today.getFullYear()
		) {
			el.classList.add('bg-yellow-400', 'rounded-full');
		}

		const monthStr = (month + 1 < 10 ? '0' : '') + (month + 1);
		const dayStr = (i < 10 ? '0' : '') + i;
		const dateStr = year + '-' + monthStr + '-' + dayStr;

		const matched = events.find(function(e) {
			return e.start === dateStr;
		});

		if (matched) {
			const img = document.createElement('img');
			img.src = '/img/paw_active.png';
			img.alt = '이벤트';
			img.className = 'w-5 h-5 absolute -top-1 -right-1';
			el.appendChild(img);
		}

		el.onclick = function() {
			const sameDayEvents = events.filter(e => e.start === dateStr);

			if (sameDayEvents.length === 1) {
				openVaccineDetailModal(sameDayEvents[0]); // 단일일 때만 바로 상세
			} else if (sameDayEvents.length > 1) {
				openEventListModal(sameDayEvents); // 목록 모달 열기
			} else {
				openVaccineModal(petId, dateStr); // 새로 등록
			}
		};

		grid.appendChild(el);
	}

	const total = firstDay + lastDate;
	const nextCells = 7 - (total % 7);
	if (nextCells < 7) {
		for (let i = 1; i <= nextCells; i++) {
			const el = document.createElement('div');
			el.textContent = i;
			el.className = 'text-gray-400';
			grid.appendChild(el);
		}
	}

	container.appendChild(grid);
	updateVaccinationList(year, month);
}
