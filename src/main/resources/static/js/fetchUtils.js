export function post(postBody) {
    return {
        method: "POST",
        body: postBody
    };
}

export function del() {
    return {
        method: "DELETE"
    };
}

export function postFavorite(isFavorite) {
    return post({
        isFavorite: isFavorite
    });
}