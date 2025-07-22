const paw = document.getElementById('cat-paw');
const btn = document.getElementById('hamburger-btn');

let isVisible = false;

btn.addEventListener('click', () => {
	isVisible = !isVisible;
	if (isVisible) {
		paw.classList.remove('left-[-100%]');
		paw.classList.add('left-0');
	} else {
		paw.classList.remove('left-0');
		paw.classList.add('left-[-100%]');
	}
});



function App() {
	const [pets, setPets] = React.useState([null])
	const [loginedMember, setLoginedMember] = React.useState(null)
	const [crew, setCrew] = React.useState(null)

	window.localStorage.setItem("loginedMemberId", loginedMember?.id);

	React.useEffect(() => {
		fetch(`/usr/member/myPage`)
			.then(res => res.json())
			.then((memberData) => {
				console.log("로그인 멤버:", memberData);
				setLoginedMember(memberData);
				window.localStorage.setItem("loginedMemberId", memberData.id);
			});
	}, []);

	React.useEffect(() => {
		fetch(`/api/pet/list?memberId=${loginedMember?.id}`)
			.then(res => res.json())
			.then((data) => {
				console.log(data)
				console.log("petlist: ", data.pets)
				setPets(data.pets || []); // ← 정확히 'pets'를 받아야 함
				setCrew(data.crews || []);
			});
	}, [loginedMember])
	// sidebar.js

};

document.querySelectorAll('.menu-item').forEach((item) => {
	item.addEventListener('click', () => {
		const page = item.dataset.page
		let url = ''
		const petId = window.localStorage.getItem('selectedPetId');
		const loginedMemberId = window.localStorage.getItem('loginedMember');
		switch (page) {
			case 'pet':
				url = `/usr/pet/petPage?petId=${petId}` // 로그인 ID로 교체 가능
				break
			case 'my':
				url = `/usr/pet/list?memberId=${loginedMemberId}`
				break
			case 'crew':
				url = `/usr/walkCrew/list`
				break
			case 'qna':
				url = `/usr/qna/list`
				break
			case 'admin':
				url = `/adm/article/list`
				break
		}

		window.parent.location.href = url
	})
})