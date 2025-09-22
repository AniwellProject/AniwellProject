// ✅ 성공 시 알림 메시지 요청
						fetch('/toast/doDelete', {
							method: 'POST'
						})
							.then(res => res.json())  // 이미 JSON 파싱됨
							.then(toastData => {
								Toast.fire({
									icon: 'success',
									title: toastData.msg
								});

								closeCommentModal?.();
								setTimeout(() => location.reload(), 1000);
							})
							.catch(err => {
								console.warn('⚠️ 응답 JSON 파싱 실패:', err);
								Toast.fire({
									icon: 'success',
									title: '!'
								});
								setTimeout(() => location.reload(), 1000);
							});